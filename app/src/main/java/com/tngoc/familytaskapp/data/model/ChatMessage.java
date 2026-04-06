package com.tngoc.familytaskapp.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.Timestamp;

public class ChatMessage {
    @DocumentId
    private String messageId;
    private String userId;   // "uid" của người dùng hoặc "bot"
    private String content;
    private Timestamp time;

    public ChatMessage() {}

    public ChatMessage(String userId, String content) {
        this.userId = userId;
        this.content = content;
        this.time = Timestamp.now();
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getTime() { return time; }
    public void setTime(Timestamp time) { this.time = time; }
}
