package com.tngoc.familytaskapp.ui.chatbot;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tngoc.familytaskapp.data.model.ChatHistory;
import com.tngoc.familytaskapp.data.model.ChatMessage;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.data.model.User;
import com.tngoc.familytaskapp.data.model.Workspace;
import com.tngoc.familytaskapp.data.repository.ChatRepository;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatBotViewModel extends ViewModel {

    private final ChatRepository chatRepository;
    private GenerativeModelFutures model;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final FirebaseFirestore db;

    public final MutableLiveData<List<ChatHistory>> historiesLiveData = new MutableLiveData<>();
    public final MutableLiveData<List<ChatMessage>> messagesLiveData = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<String>            currentHistoryId = new MutableLiveData<>();
    public final MutableLiveData<Boolean>           loadingLiveData  = new MutableLiveData<>(false);
    public final MutableLiveData<String>            errorLiveData    = new MutableLiveData<>();

    private List<Workspace> userWorkspaces = new ArrayList<>();
    private Map<String, List<Task>> workspaceTasks = new HashMap<>();
    private Map<String, User> userCache = new HashMap<>();
    private List<Task> myAssignedTasks = new ArrayList<>();
    private List<Task> myCreatedTasks = new ArrayList<>();

    public ChatBotViewModel() {
        this.chatRepository = new ChatRepository();
        this.db = FirebaseFirestore.getInstance();
        initModel(Constants.GEMINI_API_KEY);
    }

    public void initModel(String apiKey) {
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", apiKey);
        this.model = GenerativeModelFutures.from(gm);
    }

    public void loadContextData(String userId) {
        // 1. Load Workspaces
        db.collection(Constants.COLLECTION_WORKSPACES)
                .whereArrayContains("memberIds", userId)
                .get()
                .addOnSuccessListener(wsSnapshots -> {
                    userWorkspaces.clear();
                    workspaceTasks.clear();
                    for (QueryDocumentSnapshot doc : wsSnapshots) {
                        Workspace ws = doc.toObject(Workspace.class);
                        ws.setWorkspaceId(doc.getId());
                        userWorkspaces.add(ws);
                        
                        // Load members for cache
                        loadWorkspaceMembers(ws.getMemberIds());
                        
                        // Load tasks for this workspace
                        loadWorkspaceTasks(ws.getWorkspaceId());
                    }
                });

        // 2. Load Tasks specifically related to the user
        db.collectionGroup(Constants.COLLECTION_TASKS)
                .whereArrayContains("assignedToIds", userId)
                .get()
                .addOnSuccessListener(taskSnapshots -> {
                    myAssignedTasks.clear();
                    for (QueryDocumentSnapshot doc : taskSnapshots) {
                        myAssignedTasks.add(doc.toObject(Task.class));
                    }
                });

        db.collectionGroup(Constants.COLLECTION_TASKS)
                .whereEqualTo("createdBy", userId)
                .get()
                .addOnSuccessListener(taskSnapshots -> {
                    myCreatedTasks.clear();
                    for (QueryDocumentSnapshot doc : taskSnapshots) {
                        myCreatedTasks.add(doc.toObject(Task.class));
                    }
                });
    }

    private void loadWorkspaceMembers(List<String> memberIds) {
        if (memberIds == null) return;
        for (String mId : memberIds) {
            if (!userCache.containsKey(mId)) {
                db.collection(Constants.COLLECTION_USERS).document(mId).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) userCache.put(mId, doc.toObject(User.class));
                        });
            }
        }
    }

    private void loadWorkspaceTasks(String wsId) {
        db.collection(Constants.COLLECTION_WORKSPACES).document(wsId)
                .collection(Constants.COLLECTION_TASKS).get()
                .addOnSuccessListener(snapshots -> {
                    List<Task> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(Task.class));
                    }
                    workspaceTasks.put(wsId, list);
                });
    }

    public void loadHistories(String userId) {
        chatRepository.getChatHistories(userId, historiesLiveData, errorLiveData);
    }

    public void selectHistory(String userId, String historyId) {
        currentHistoryId.setValue(historyId);
        chatRepository.getMessages(userId, historyId, messagesLiveData, errorLiveData);
    }

    public void resetChat() {
        currentHistoryId.setValue(null);
        messagesLiveData.setValue(new ArrayList<>());
    }

    public void startNewChat(String userId, String firstMessage) {
        String header = firstMessage.length() > 30 ? firstMessage.substring(0, 27) + "..." : firstMessage;
        ChatHistory history = new ChatHistory(header);
        
        MutableLiveData<String> historyIdLD = new MutableLiveData<>();
        historyIdLD.observeForever(id -> {
            if (id != null) {
                currentHistoryId.setValue(id);
                sendMessage(userId, firstMessage);
                loadHistories(userId);
            }
        });
        chatRepository.createChatHistory(userId, history, historyIdLD, errorLiveData);
    }

    public void sendMessage(String userId, String content) {
        String historyId = currentHistoryId.getValue();
        if (historyId == null) {
            startNewChat(userId, content);
            return;
        }

        ChatMessage userMsg = new ChatMessage(userId, content);
        chatRepository.saveMessage(userId, historyId, userMsg, new MutableLiveData<>(), errorLiveData);
        
        List<ChatMessage> currentMsgs = messagesLiveData.getValue();
        if (currentMsgs == null) currentMsgs = new ArrayList<>();
        currentMsgs.add(userMsg);
        messagesLiveData.setValue(currentMsgs);

        callGemini(userId, historyId, content);
    }

    private void callGemini(String userId, String historyId, String userPrompt) {
        loadingLiveData.setValue(true);

        StringBuilder sb = new StringBuilder();
        sb.append("Dữ liệu hệ thống của người dùng (ID: ").append(userId).append("):\n\n");
        
        sb.append("1. CÁC WORKSPACE ĐANG THAM GIA:\n");
        for (Workspace ws : userWorkspaces) {
            sb.append("- ").append(ws.getName()).append(" (ID: ").append(ws.getWorkspaceId()).append(")\n");
            sb.append("  Thành viên: ");
            for (String mId : ws.getMemberIds()) {
                User u = userCache.get(mId);
                sb.append(u != null ? u.getDisplayName() : mId).append(", ");
            }
            sb.append("\n");
        }

        sb.append("\n2. DANH SÁCH NHIỆM VỤ (TASKS):\n");
        for (Map.Entry<String, List<Task>> entry : workspaceTasks.entrySet()) {
            String wsName = "Workspace ID " + entry.getKey();
            for (Workspace ws : userWorkspaces) if (ws.getWorkspaceId().equals(entry.getKey())) wsName = ws.getName();
            
            sb.append("- Tại ").append(wsName).append(":\n");
            for (Task t : entry.getValue()) {
                sb.append("  + [").append(t.getStatus()).append("] ").append(t.getTitle())
                  .append(" (Giao cho: ").append(t.getAssignedToIds()).append(")\n");
            }
        }

        sb.append("\n3. LỊCH SỬ CÁ NHÂN:\n");
        sb.append("- Nhiệm vụ bạn được giao: ");
        for (Task t : myAssignedTasks) sb.append(t.getTitle()).append(" (").append(t.getStatus()).append("), ");
        sb.append("\n- Nhiệm vụ bạn đã tạo: ");
        for (Task t : myCreatedTasks) sb.append(t.getTitle()).append(" (").append(t.getStatus()).append("), ");

        String systemPrompt = "Bạn là trợ lý ảo AI thông minh của ứng dụng 'Family Task App'. " +
                "Dưới đây là ngữ cảnh dữ liệu thực tế của người dùng hiện tại:\n" + sb.toString() + "\n" +
                "QUY TẮC TRẢ LỜI:\n" +
                "1. Ưu tiên sử dụng dữ liệu trên để trả lời câu hỏi của người dùng.\n" +
                "2. Xưng hô thân thiện (ví dụ: 'Chào bạn', 'Mình có thể giúp gì...').\n" +
                "3. Nếu người dùng hỏi về ai đó trong workspace, hãy tìm tên họ trong danh sách thành viên.\n" +
                "4. Nếu người dùng hỏi về công việc, hãy tổng hợp từ mục Tasks và Lịch sử cá nhân.\n" +
                "5. Chỉ trả lời các vấn đề liên quan đến ứng dụng và công việc. Từ chối các chủ đề nhạy cảm hoặc không liên quan.";

        Content inputContent = new Content.Builder()
                .addText(systemPrompt + "\n\nUser: " + userPrompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(inputContent);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String botReply = result.getText();
                ChatMessage botMsg = new ChatMessage("bot", botReply);
                chatRepository.saveMessage(userId, historyId, botMsg, new MutableLiveData<>(), errorLiveData);
                
                List<ChatMessage> currentMsgs = messagesLiveData.getValue();
                currentMsgs.add(botMsg);
                messagesLiveData.postValue(currentMsgs);
                loadingLiveData.postValue(false);
            }

            @Override
            public void onFailure(Throwable t) {
                errorLiveData.postValue("Lỗi kết nối AI: " + t.getMessage());
                loadingLiveData.postValue(false);
            }
        }, executor);
    }
}
