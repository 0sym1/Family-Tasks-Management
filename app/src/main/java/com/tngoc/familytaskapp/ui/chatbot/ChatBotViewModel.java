package com.tngoc.familytaskapp.ui.chatbot;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tngoc.familytaskapp.data.model.ChatMessage;
import com.tngoc.familytaskapp.data.repository.ChatRepository;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ChatBotViewModel extends ViewModel {

    private final ChatRepository chatRepository;

    public final MutableLiveData<List<ChatMessage>> messagesLiveData = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<Boolean>           loadingLiveData  = new MutableLiveData<>(false);
    public final MutableLiveData<String>            errorLiveData    = new MutableLiveData<>();

    public ChatBotViewModel() {
        this.chatRepository = new ChatRepository();
    }

    public void loadHistory(String userId) {
        chatRepository.getChatHistory(userId, messagesLiveData, errorLiveData);
    }

    public void sendMessage(String userId, String content) {
        List<ChatMessage> current = messagesLiveData.getValue();
        if (current == null) current = new ArrayList<>();

        // Thêm tin nhắn user vào danh sách
        ChatMessage userMsg = new ChatMessage(userId, Constants.CHAT_ROLE_USER, content);
        current.add(userMsg);
        messagesLiveData.setValue(new ArrayList<>(current));

        // Lưu tin nhắn user lên Firestore
        MutableLiveData<String> msgId = new MutableLiveData<>();
        chatRepository.saveMessage(userId, userMsg, msgId, errorLiveData);

        // TODO: Gọi Gemini/OpenAI API để lấy phản hồi bot
        loadingLiveData.setValue(true);
    }

    public void receiveBotReply(String userId, String botContent) {
        List<ChatMessage> current = messagesLiveData.getValue();
        if (current == null) current = new ArrayList<>();

        ChatMessage botMsg = new ChatMessage(userId, Constants.CHAT_ROLE_BOT, botContent);
        current.add(botMsg);
        messagesLiveData.setValue(new ArrayList<>(current));

        MutableLiveData<String> msgId = new MutableLiveData<>();
        chatRepository.saveMessage(userId, botMsg, msgId, errorLiveData);

        loadingLiveData.setValue(false);
    }

    public void clearHistory(String userId) {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        chatRepository.clearChatHistory(userId, success, errorLiveData);
        messagesLiveData.setValue(new ArrayList<>());
    }
}

