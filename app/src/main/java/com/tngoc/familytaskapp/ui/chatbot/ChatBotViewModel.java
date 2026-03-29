package com.tngoc.familytaskapp.ui.chatbot;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.tngoc.familytaskapp.data.model.ChatHistory;
import com.tngoc.familytaskapp.data.model.ChatMessage;
import com.tngoc.familytaskapp.data.model.Task;
import com.tngoc.familytaskapp.data.model.Workspace;
import com.tngoc.familytaskapp.data.repository.ChatRepository;
import com.tngoc.familytaskapp.data.repository.TaskRepository;
import com.tngoc.familytaskapp.data.repository.WorkspaceRepository;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatBotViewModel extends ViewModel {

    private final ChatRepository chatRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceRepository workspaceRepository;
    private GenerativeModelFutures model;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public final MutableLiveData<List<ChatHistory>> historiesLiveData = new MutableLiveData<>();
    public final MutableLiveData<List<ChatMessage>> messagesLiveData = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<String>            currentHistoryId = new MutableLiveData<>();
    public final MutableLiveData<Boolean>           loadingLiveData  = new MutableLiveData<>(false);
    public final MutableLiveData<String>            errorLiveData    = new MutableLiveData<>();

    private List<Task> contextTasks = new ArrayList<>();
    private List<Workspace> contextWorkspaces = new ArrayList<>();

    public ChatBotViewModel() {
        this.chatRepository = new ChatRepository();
        this.taskRepository = new TaskRepository();
        this.workspaceRepository = new WorkspaceRepository();
        
        initModel(Constants.GEMINI_API_KEY);
    }

    public void initModel(String apiKey) {
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", apiKey);
        this.model = GenerativeModelFutures.from(gm);
    }

    public void loadContextData(String userId) {
        // Load workspaces first
        MutableLiveData<List<Workspace>> workspacesLD = new MutableLiveData<>();
        workspacesLD.observeForever(workspaces -> {
            if (workspaces != null) {
                this.contextWorkspaces = workspaces;
                for (Workspace ws : workspaces) {
                    taskRepository.getTasksInWorkspace(ws.getWorkspaceId(), new MutableLiveData<>(), errorLiveData);
                }
            }
        });
        workspaceRepository.getWorkspacesForUser(userId, workspacesLD, errorLiveData);
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

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Hệ thống có các Workspace sau: ");
        for (Workspace ws : contextWorkspaces) {
            contextBuilder.append(ws.getName()).append(" (Thành viên: ").append(ws.getMemberIds().size()).append("), ");
        }

        String systemPrompt = "Bạn là trợ lý ảo cho ứng dụng Family Task App. " +
                "Dữ liệu hiện tại: " + contextBuilder.toString() + ". " +
                "QUY TẮC: Chỉ được trả lời các câu hỏi về tasks, workspaces và con người trong hệ thống. " +
                "Nếu người dùng hỏi về chủ đề khác, hãy từ chối lịch sự và hướng dẫn họ quay lại chủ đề chính.";

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
                errorLiveData.postValue("Lỗi Gemini: " + t.getMessage());
                loadingLiveData.postValue(false);
            }
        }, executor);
    }
}
