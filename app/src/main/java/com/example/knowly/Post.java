package com.example.knowly;

import java.util.List;
import java.util.Map;

public class Post {
    private String content;
    private String author;
    private String postId;
    private List<String> categories;
    private long timestamp;

    private Map<String, Boolean> upvotes;
    private Map<String, Boolean> downvotes;
    private int comment_num;

    public Post() {}

    // --- Getters ---
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public String getPostId() { return postId; }
    public List<String> getCategories() { return categories; }
    public long getTimestamp() { return timestamp; }

    // --- Dynamic Calculation for UI ---
    public int getUpvote_num() {
        return (upvotes != null) ? upvotes.size() : 0;
    }

    public int getDownvote_num() {
        return (downvotes != null) ? downvotes.size() : 0;
    }

    public int getComment_num() { return comment_num; }

    public Map<String, Boolean> getUpvotes() { return upvotes; }
    public Map<String, Boolean> getDownvotes() { return downvotes; }

    // --- Setters ---
    public void setPostId(String postId) { this.postId = postId; }
    public void setContent(String content) { this.content = content; }
    public void setAuthor(String author) { this.author = author; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setComment_num(int comment_num) { this.comment_num = comment_num; }
    public void setUpvotes(Map<String, Boolean> upvotes) { this.upvotes = upvotes; }
    public void setDownvotes(Map<String, Boolean> downvotes) { this.downvotes = downvotes; }
}