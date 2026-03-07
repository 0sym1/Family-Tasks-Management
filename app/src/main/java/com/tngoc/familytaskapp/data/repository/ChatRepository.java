package com.tngoc.familytaskapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tngoc.familytaskapp.data.model.ChatMessage;
import com.tngoc.familytaskapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ChatRepository {

    private final FirebaseFirestore db;

    public ChatRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void saveMessage(String userId, ChatMessage message, MutableLiveData<String> messageIdLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_CHAT_HISTORY)
                .document(userId)
                .collection(Constants.COLLECTION_MESSAGES)
                .add(message)
                .addOnSuccessListener(ref -> messageIdLiveData.setValue(ref.getId()))
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void getChatHistory(String userId, MutableLiveData<List<ChatMessage>> messagesLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_CHAT_HISTORY)
                .document(userId)
                .collection(Constants.COLLECTION_MESSAGES)
                .orderBy("timestamp")
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

    public void clearChatHistory(String userId, MutableLiveData<Boolean> successLiveData, MutableLiveData<String> errorLiveData) {
        db.collection(Constants.COLLECTION_CHAT_HISTORY)
                .document(userId)
                .collection(Constants.COLLECTION_MESSAGES)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        doc.getReference().delete();
                    }
                    successLiveData.setValue(true);
                })
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }
}

