package com.example.knowly;

public class NotificationItem {
    private String username;
    private String action;
    private String time;
    private int iconRes;

    public NotificationItem(String username, String action, String time, int iconRes) {
        this.username = username;
        this.action = action;
        this.time = time;
        this.iconRes = iconRes;
    }

    public String getUsername() { return username; }
    public String getAction() { return action; }
    public String getTime() { return time; }
    public int getIconRes() { return iconRes; }
}