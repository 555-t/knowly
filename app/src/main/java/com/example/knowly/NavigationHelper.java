package com.example.knowly;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView; // Import TextView

// Firebase Imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class NavigationHelper {

    public static void setupNavigation(final Activity activity) {

        // 1. Existing Navigation Logic

        // navigate to home_page.xml
        View navHome = activity.findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> navigateTo(activity, HomePage.class));
        }

        // navigate to fragment_weeklyfeatured.xml
        View navWeekly = activity.findViewById(R.id.navWeekly);
        if (navWeekly != null) {
            navWeekly.setOnClickListener(v -> navigateTo(activity, WeeklyFeaturedActivity.class));
        }

        // navigate to activity_userpage
        View navProfile = activity.findViewById(R.id.nav_profile_streak);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> navigateTo(activity, UserPageActivity.class));
        }

        // navigate to fragment_search.xml
        View navSearch = activity.findViewById(R.id.navSearch);
        if (navSearch != null) {
            navSearch.setOnClickListener(v -> navigateTo(activity, SearchActivity.class));
        }

        // navigate to notifications_activity.xml
        View navNotif = activity.findViewById(R.id.navNotifications);
        if (navNotif != null) {
            navNotif.setOnClickListener(v -> navigateTo(activity, NotificationActivity.class));
        }

        // 2. NEW: Auto-Update Navigation Avatar Initial
        updateNavAvatar(activity);
    }

    public static void updateNavAvatar(Activity activity) {
        TextView tvNavAvatarText = activity.findViewById(R.id.tvNavAvatarText);

        if (tvNavAvatarText != null) {
            // 1. INSTANT FIX: Load from local memory first to stop the glitching
            String cachedName = activity.getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE)
                    .getString("current_username", "");

            if (!cachedName.isEmpty()) {
                tvNavAvatarText.setText(String.valueOf(cachedName.charAt(0)).toUpperCase());
            }

            // 2. BACKGROUND SYNC: Update from Firebase in case the name changed recently
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.getUid())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String username = documentSnapshot.getString("username");
                                if (username != null && !username.isEmpty()) {
                                    String initial = String.valueOf(username.charAt(0)).toUpperCase();

                                    // Only update UI if it's different to prevent flickering
                                    if (!tvNavAvatarText.getText().toString().equals(initial)) {
                                        tvNavAvatarText.setText(initial);
                                    }

                                    // Save locally for the NEXT activity/session
                                    activity.getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE)
                                            .edit()
                                            .putString("current_username", username)
                                            .apply();
                                }
                            }
                        });
            }
        }
    }
    private static void navigateTo(Activity activity, Class<?> targetClass) {
        // Don't restart the activity if we are already on it
        if (activity.getClass().equals(targetClass)) return;

        Intent intent = new Intent(activity, targetClass);
        // Brings existing activity to front instead of creating new ones (prevents back-button mess)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0); // Smooth "tab" transition
    }
}