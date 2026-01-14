package com.example.knowly;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class NotificationItem {
    private String username;
    private String action;
    private String time;
    private String iconRes;

    @ServerTimestamp // This tells Firebase to handle the timing
    private Timestamp timestamp;

    // MANDATORY: Firebase needs this empty constructor
    public NotificationItem() {}

    public NotificationItem(String username, String action, String time, String iconRes) {
        this.username = username;
        this.action = action;
        this.time = time;
        this.iconRes = iconRes;
    }

    public String getUsername() { return username; }
    public String getAction() { return action; }
    public String getTime() { return time; }
    public String getIconRes() { return iconRes; }
    public Timestamp getTimestamp() { return timestamp; }
}