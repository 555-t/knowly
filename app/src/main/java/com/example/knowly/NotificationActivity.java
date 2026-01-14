package com.example.knowly;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity);

        // 1. Navigation Setup
        NavigationHelper.setupNavigation(this);

        // 2. RecyclerView Setup
        recyclerView = findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        // Add one starting notification so it's not empty
        notificationList.add(new NotificationItem("sarah_smith", "started following you", "2h ago", R.drawable.user_following_svgrepo_com));

        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        // 3. Popup Setup
        createNotificationChannel();

        // Check Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // 4. Test: Wait 3 seconds, then add a notification and show popup
        //new Handler().postDelayed(() -> {
         //   addNewNotification("sarah_smith", "commented on your post", "Just now", android.R.drawable.stat_notify_chat);
        //}, 3000);
    }

    public void addNewNotification(String user, String action, String time, int icon) {
        // Add new item to the top of the list
        notificationList.add(0, new NotificationItem(user, action, time, icon));
        adapter.notifyItemInserted(0);
        recyclerView.scrollToPosition(0);

        // Show the popup
        showPopupNotification("New Interaction!", user + " " + action);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("KNOWLY_CHANNEL", "User Alerts", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
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
        if (manager != null) manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}