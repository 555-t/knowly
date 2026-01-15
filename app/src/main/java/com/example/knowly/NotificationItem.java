package com.example.knowly;

import com.google.firebase.Timestamp;

public class NotificationItem {
    private String username;
    private String action;
    private String type;
    private Timestamp timestamp; // Use Timestamp for Firestore compatibility
    private String fromUserId;
    private String postId;

    public NotificationItem() {}

    // Updated Constructor
    public NotificationItem(String username, String action, String type, Timestamp timestamp, String fromUserId) {
        this.username = username;
        this.action = action;
        this.type = type;
        this.timestamp = timestamp;
        this.fromUserId = fromUserId;
    }

    // --- Getters ---
    public String getUsername() { return username; }
    public String getAction() { return action; }
    public String getType() { return type; }
    public Timestamp getTimestamp() { return timestamp; } // Returns Timestamp
    public String getFromUserId() { return fromUserId; }
    public String getPostId() { return postId; }

    // --- Setters ---
    public void setUsername(String username) { this.username = username; }
    public void setAction(String action) { this.action = action; }
    public void setType(String type) { this.type = type; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }
    public void setPostId(String postId) { this.postId = postId; }

    // Helper to get long milliseconds for DateUtils
    public long getTimestampMillis() {
        return (timestamp != null) ? timestamp.toDate().getTime() : 0;
    }
}