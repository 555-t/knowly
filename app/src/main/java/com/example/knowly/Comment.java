package com.example.knowly;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class Comment {
    private String text;
    private String authorId;
    private long timestamp; // We will convert Firestore Timestamp to this long

    // Required empty constructor for Firestore
    public Comment() {}

    public Comment(String text, String authorId, long timestamp) {
        this.text = text;
        this.authorId = authorId;
        this.timestamp = timestamp;
    }

    // --- Getters ---
    public String getText() { return text; }
    public String getAuthorId() { return authorId; }
    public long getTimestamp() { return timestamp; }

    // --- Setters (CRITICAL FOR FIRESTORE) ---
    public void setText(String text) { this.text = text; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    /**
     * This setter is the "secret sauce."
     * If Firestore sends a Timestamp object, this converts it to long
     * so your DateUtils in the Adapter doesn't crash.
     */
    public void setTimestamp(Object timestamp) {
        if (timestamp instanceof Timestamp) {
            this.timestamp = ((Timestamp) timestamp).toDate().getTime();
        } else if (timestamp instanceof Long) {
            this.timestamp = (Long) timestamp;
        }
    }
}