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

    private static void updateNavAvatar(Activity activity) {
        // Find the TextView inside the bottom nav bar (using the ID you added earlier)
        TextView tvNavAvatarText = activity.findViewById(R.id.tvNavAvatarText);

        if (tvNavAvatarText != null) {
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
                                    // Get first letter and capitalize it
                                    String initial = String.valueOf(username.charAt(0)).toUpperCase();
                                    tvNavAvatarText.setText(initial);
                                } else {
                                    tvNavAvatarText.setText("U");
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Fail silently so we don't crash or annoy user on other pages
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