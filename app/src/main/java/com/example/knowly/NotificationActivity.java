package com.example.knowly;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity);

        // 1. Navigation & UI Setup FIRST
        NavigationHelper.setupNavigation(this);

        recyclerView = findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        // 2. System Setup (Permissions/Channels)
        createNotificationChannel();
        checkNotificationPermission();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        // 3. Firebase Initialization & Safety Check
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // Start listening ONLY once here
            listenForNotifications();
        } else {
            Log.e("KNOWLY", "User is not logged in!");
            // Optional: Redirect to login so the user doesn't see a blank screen
            // startActivity(new Intent(this, LoginActivity.class));
            // finish();
        }
    }

    private void listenForNotifications() {
        if (userId == null) return;

        // Note: Make sure "timestamp" field exists in Firestore or the query will return nothing
        db.collection("users").document(userId).collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("KNOWLY", "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                NotificationItem item = dc.getDocument().toObject(NotificationItem.class);

                                // Add to top of list
                                notificationList.add(0, item);
                                adapter.notifyItemInserted(0);
                                recyclerView.scrollToPosition(0);

                                // Trigger System Popup
                                showPopupNotification("KNOWLY", item.getUsername() + " " + item.getAction());
                            }
                        }
                    } else {
                        Log.d("KNOWLY", "No notifications found for user: " + userId);
                    }
                });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Importance must be HIGH for the popup to appear
            NotificationChannel channel = new NotificationChannel(
                    "KNOWLY_CHANNEL",
                    "User Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for post comments and interactions");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void showPopupNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "KNOWLY_CHANNEL")
                .setSmallIcon(android.R.drawable.stat_notify_chat) // Ensure this icon exists
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}