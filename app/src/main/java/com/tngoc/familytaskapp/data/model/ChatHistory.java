package com.tngoc.familytaskapp.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class ChatHistory {
    @DocumentId
    private String historyId;
    private String header;
    private Timestamp timestamp;

    public ChatHistory() {}

    public ChatHistory(String header) {
        this.header = header;
        this.timestamp = Timestamp.now();
    }

    public String getHistoryId() { return historyId; }
    public void setHistoryId(String historyId) { this.historyId = historyId; }

    public String getHeader() { return header; }
    public void setHeader(String header) { this.header = header; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
