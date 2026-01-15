package com.example.knowly;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class BaseActivity extends AppCompatActivity {

    // Global Timer Variables
    private Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable timeRunnable;
    private static final long TIME_INTERVAL = 60000; // 1 Minute

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupTimeTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start counting whenever ANY activity is open
        if (timeRunnable != null) {
            timeHandler.postDelayed(timeRunnable, TIME_INTERVAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop counting when activity is hidden (to save battery/avoid duplicates)
        if (timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }

    private void setupTimeTracker() {
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                updateMinutesInFirestore();
                timeHandler.postDelayed(this, TIME_INTERVAL);
            }
        };
    }

    private void updateMinutesInFirestore() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        long totalMinutes = documentSnapshot.contains("totalMinutes") ? documentSnapshot.getLong("totalMinutes") : 0;
                        long todayMinutes = documentSnapshot.contains("todayMinutes") ? documentSnapshot.getLong("todayMinutes") : 0;
                        long currentStreak = documentSnapshot.contains("currentStreak") ? documentSnapshot.getLong("currentStreak") : 0;

                        String lastActiveDate = documentSnapshot.getString("lastActiveDate");
                        String lastStreakDate = documentSnapshot.getString("lastStreakDate");

                        // Reset daily counter if it's a new day
                        if (lastActiveDate == null || !lastActiveDate.equals(todayDate)) {
                            todayMinutes = 0;
                        }

                        // Increment
                        totalMinutes++;
                        todayMinutes++;

                        // Streak Logic (30 min goal)
                        if (todayMinutes >= 30) {
                            if (lastStreakDate == null || !lastStreakDate.equals(todayDate)) {
                                // Check if consecutive day
                                String yesterday = getYesterdayDate();
                                if (yesterday.equals(lastStreakDate)) {
                                    currentStreak++;
                                } else {
                                    currentStreak = 1;
                                }
                                lastStreakDate = todayDate;
                            }
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("totalMinutes", totalMinutes);
                        updates.put("todayMinutes", todayMinutes);
                        updates.put("lastActiveDate", todayDate);
                        updates.put("currentStreak", currentStreak);
                        if (lastStreakDate != null) updates.put("lastStreakDate", lastStreakDate);

                        db.collection("users").document(uid).set(updates, SetOptions.merge());
                    }
                });
    }

    private String getYesterdayDate() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DATE, -1);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
    }
}
