package com.example.knowly;

public class Comment {
    private String text;
    private String authorId;
    private long timestamp;

    // Required empty constructor for Firebase
    public Comment() {}

    public Comment(String text, String authorId, long timestamp) {
        this.text = text;
        this.authorId = authorId;
        this.timestamp = timestamp;
    }

    public String getText() { return text; }
    public String getAuthorId() { return authorId; }
    public long getTimestamp() { return timestamp; }
}