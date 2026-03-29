package com.tngoc.familytaskapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tngoc.familytaskapp.data.model.ChatHistory;
import com.tngoc.familytaskapp.data.model.ChatMessage;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ChatRepository {

    private final FirebaseFirestore db;

    public ChatRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void createChatHistory(String userId, ChatHistory history, MutableLiveData<String> historyIdLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_CHAT_HISTORY)
                .add(history)
                .addOnSuccessListener(ref -> historyIdLiveData.setValue(ref.getId()))
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void getChatHistories(String userId, MutableLiveData<List<ChatHistory>> historiesLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_CHAT_HISTORY)
                .orderBy("timestamp", Query.Direction.DESCENDING) // Sắp xếp mới nhất lên đầu
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<ChatHistory> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(ChatHistory.class));
                    }
                    historiesLiveData.setValue(list);
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void saveMessage(String userId, String historyId, ChatMessage message, MutableLiveData<String> messageIdLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_CHAT_HISTORY)
                .document(historyId)
                .collection(Constants.COLLECTION_MESSAGES)
                .add(message)
                .addOnSuccessListener(ref -> messageIdLiveData.setValue(ref.getId()))
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void getMessages(String userId, String historyId, MutableLiveData<List<ChatMessage>> messagesLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_CHAT_HISTORY)
                .document(historyId)
                .collection(Constants.COLLECTION_MESSAGES)
                .orderBy("time")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<ChatMessage> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(ChatMessage.class));
                    }
                    messagesLiveData.setValue(list);
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }
}
