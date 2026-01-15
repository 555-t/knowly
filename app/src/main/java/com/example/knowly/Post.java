package com.example.knowly;

import com.google.firebase.Timestamp;
import java.util.List;

public class Post {
    private String author;
    private String content;
    private List<String> categories;
    private long upvote_num;
    private long downvote_num;
    private long comment_num;
    private String postId;
    private long timestamp;

    public Post() {}

    public Post(String author, String content, List<String> categories) {
        this.author = author;
        this.content = content;
        this.categories = categories;
        this.upvote_num = 0;
        this.downvote_num = 0;
        this.comment_num = 0;
    }

    // --- Basic Getters and Setters ---
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public long getUpvote_num() { return upvote_num; }
    public void setUpvote_num(long upvote_num) { this.upvote_num = upvote_num; }

    public long getDownvote_num() { return downvote_num; }
    public void setDownvote_num(long downvote_num) { this.downvote_num = downvote_num; }

    public long getComment_num() { return comment_num; }
    public void setComment_num(long comment_num) { this.comment_num = comment_num; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public long getTimestamp() { return timestamp; }

    /**
     * FIXED SETTER: Handles the conversion from Firestore's Timestamp object
     * to a long milliseconds value used by your Adapter's getTimeAgo logic.
     */
    public void setTimestamp(Object timestamp) {
        if (timestamp instanceof Timestamp) {
            this.timestamp = ((Timestamp) timestamp).toDate().getTime();
        } else if (timestamp instanceof Long) {
            this.timestamp = (Long) timestamp;
        }
    }
}