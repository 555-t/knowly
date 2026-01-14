package com.example.knowly;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class NavigationHelper {

    public static void setupNavigation(final Activity activity) {

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