package com.example.knowly;

public class NotificationItem {
    private String username;
    private String action;
    private String iconRes;
    private String type;      // IMPORTANT: "follow" or "comment"
    private long timestamp;   // Changed from Timestamp to long for RTDB
    private String fromUserId;
    private String postId;    // To know which post was commented on

    // MANDATORY: Firebase needs this empty constructor
    public NotificationItem() {}

    // Constructor for creating new notifications
    public NotificationItem(String username, String action, String type, long timestamp, String fromUserId) {
        this.username = username;
        this.action = action;
        this.type = type;
        this.timestamp = timestamp;
        this.fromUserId = fromUserId;
    }

    // --- Getters ---
    public String getUsername() { return username; }
    public String getAction() { return action; }
    public String getIconRes() { return iconRes; }
    public String getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public String getFromUserId() { return fromUserId; }
    public String getPostId() { return postId; }

    // --- Setters (Required for Firebase RTDB to map data) ---
    public void setUsername(String username) { this.username = username; }
    public void setAction(String action) { this.action = action; }
    public void setIconRes(String iconRes) { this.iconRes = iconRes; }
    public void setType(String type) { this.type = type; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }
    public void setPostId(String postId) { this.postId = postId; }
}