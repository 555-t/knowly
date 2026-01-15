package com.example.knowly;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class NotificationUtils {

    /**
     * Sends a notification to a specific user.
     * @param receiverId The UID of the person receiving the notification.
     * @param type "comment", "follow", or "upvote".
     * @param action The text to display (e.g., "commented on your post").
     * @param postId The ID of the post involved (optional, can be null).
     */
    public static void sendNotification(String receiverId, String type, String action, String postId) {
        String currentUserId = FirebaseAuth.getInstance().getUid();

        // Don't notify yourself (e.g., if you comment on your own post)
        if (currentUserId == null || currentUserId.equals(receiverId)) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> notifMap = new HashMap<>();
        notifMap.put("type", type);
        notifMap.put("action", action);
        notifMap.put("fromUserId", currentUserId); // Matches NotificationItem
        notifMap.put("postId", postId);
        notifMap.put("timestamp", FieldValue.serverTimestamp());

        // Path: users -> [RECEIVER_UID] -> notifications -> [AUTO_ID]
        db.collection("users")
                .document(receiverId)
                .collection("notifications")
                .add(notifMap);
    }
}