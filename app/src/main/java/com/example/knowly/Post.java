package com.example.knowly;

import java.util.List;
import java.util.Map;

public class Post {
    private String content;
    private String author; // This stores the UID of the creator
    private String postId;
    private List<String> categories; // This stores the multiple categories
    private long timestamp;

    private Map<String, Boolean> upvotes;
    private Map<String, Boolean> downvotes;
    private int comment_num;

    // Required empty constructor for Firebase
    public Post() {}

    // --- Getters ---
    public String getContent() { return content; }

    // Crucial for the "Following" filter in HomePage.java
    public String getAuthor() { return author; }

    public String getPostId() { return postId; }

    // Crucial for the "For You" filter in HomePage.java
    public List<String> getCategories() { return categories; }

    public long getTimestamp() { return timestamp; }

    // --- Vote Logic Getters ---
    public int getUpvote_num() {
        return (upvotes != null) ? upvotes.size() : 0;
    }

    public int getDownvote_num() {
        return (downvotes != null) ? downvotes.size() : 0;
    }

    public int getComment_num() { return comment_num; }

    // --- The Maps ---
    public Map<String, Boolean> getUpvotes() { return upvotes; }
    public Map<String, Boolean> getDownvotes() { return downvotes; }

    // --- Setters (Essential for Firebase Data Mapping) ---
    public void setPostId(String postId) { this.postId = postId; }
    public void setContent(String content) { this.content = content; }
    public void setAuthor(String author) { this.author = author; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setComment_num(int comment_num) { this.comment_num = comment_num; }
    public void setUpvotes(Map<String, Boolean> upvotes) { this.upvotes = upvotes; }
    public void setDownvotes(Map<String, Boolean> downvotes) { this.downvotes = downvotes; }
}