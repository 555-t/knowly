package com.example.knowly;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;

    private DatabaseReference dbRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity);

        // 1. Navigation & UI Setup
        // This MUST stay here to keep the bottom nav working
        NavigationHelper.setupNavigation(this);

        recyclerView = findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        // 2. System Setup (Permissions/Channels)
        createNotificationChannel();
        checkNotificationPermission();

        // 3. Firebase Realtime Database Initialization
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // Point to Realtime Database "Notifications" node
            dbRef = FirebaseDatabase.getInstance().getReference("Notifications").child(userId);
            listenForNotifications();
        } else {
            Log.e("KNOWLY", "User is not logged in!");
        }
    }

    private void listenForNotifications() {
        if (userId == null) return;

        // Using ValueEventListener for Realtime Database
        // .limitToLast(50) ensures we don't load thousands of old notifications
        dbRef.orderByChild("timestamp").limitToLast(50)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            NotificationItem item = ds.getValue(NotificationItem.class);
                            if (item != null) {
                                // Add to index 0 so newest appears at the top
                                notificationList.add(0, item);
                            }
                        }
                        adapter.notifyDataSetChanged();

                        // If the list is updated, show a popup for the very latest one
                        if (!notificationList.isEmpty()) {
                            NotificationItem latest = notificationList.get(0);
                            // Optional: only show popup if the timestamp is very recent (within last 10 seconds)
                            long now = System.currentTimeMillis();
                            if (now - latest.getTimestamp() < 10000) {
                                showPopupNotification("KNOWLY", latest.getUsername() + " " + latest.getAction());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("KNOWLY", "Database error: " + error.getMessage());
                    }
                });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "KNOWLY_CHANNEL",
                    "User Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for interactions");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void showPopupNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "KNOWLY_CHANNEL")
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}