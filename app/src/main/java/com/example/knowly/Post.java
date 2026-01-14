package com.example.knowly;

import java.util.List;
import java.util.Map;

public class Post {
    private String content;
    private String author;
    private String postId;
    private List<String> categories;

    // The new Map-based system
    private Map<String, Boolean> upvotes;
    private Map<String, Boolean> downvotes;
    private int comment_num;

    public Post() {} // Required for Firebase

    // --- Content Getters ---
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public String getPostId() { return postId; }
    public List<String> getCategories() { return categories; }

    // --- Vote Logic Getters ---
    // These calculate the size of the Map to give the number
    public int getUpvote_num() {
        return (upvotes != null) ? upvotes.size() : 0;
    }

    public int getDownvote_num() {
        return (downvotes != null) ? downvotes.size() : 0;
    }

    public int getComment_num() { return comment_num; }

    // --- The Maps (Used by the Adapter to check for "Toggling") ---
    public Map<String, Boolean> getUpvotes() { return upvotes; }
    public Map<String, Boolean> getDownvotes() { return downvotes; }
}