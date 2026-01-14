package com.example.knowly;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationUtils {

    /**
     * Sends a notification to a specific user in the Realtime Database.
     * @param receiverId The UID of the person receiving the notification.
     * @param type Either "comment" or "follow" (determines which XML layout is used).
     * @param actionText The message (e.g., "upvoted your post" or "started following you").
     */
    public static void sendNotification(String receiverId, String type, String actionText) {
        String currentUserId = FirebaseAuth.getInstance().getUid();

        // 1. Safety Check: Don't notify if user is not logged in or notifying themselves
        if (currentUserId == null || currentUserId.equals(receiverId)) {
            return;
        }

        // 2. Fetch the current user's name to show in the notification
        FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId)
                .child("username")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String senderName = "Someone";
                    if (snapshot.exists() && snapshot.getValue() != null) {
                        senderName = snapshot.getValue().toString();
                    }

                    // 3. Create the notification reference for the receiver
                    DatabaseReference notifRef = FirebaseDatabase.getInstance()
                            .getReference("Notifications")
                            .child(receiverId);

                    String notifId = notifRef.push().getKey();

                    // 4. Build the Notification object
                    NotificationItem item = new NotificationItem();
                    item.setUsername(senderName);
                    item.setAction(actionText);
                    item.setType(type);
                    item.setTimestamp(System.currentTimeMillis()); // Local time for immediate display
                    item.setFromUserId(currentUserId);

                    // 5. Save to Database
                    if (notifId != null) {
                        notifRef.child(notifId).setValue(item);
                    }
                });
    }
}