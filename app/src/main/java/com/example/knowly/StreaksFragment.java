package com.example.knowly;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class StreaksFragment extends Fragment {

    private TextView tvTotalMinutes, tvTodayMinutes, tvStreakTitle;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_streaks, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvTotalMinutes = view.findViewById(R.id.tvTotalMinutes);
        tvTodayMinutes = view.findViewById(R.id.tvTodayMinutes);
        tvStreakTitle = view.findViewById(R.id.tvStreakTitle);

        loadStreakData();

        return view;
    }

    private void loadStreakData() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("users").document(uid)
                .addSnapshotListener((doc, error) -> {
                    if (doc != null && doc.exists()) {
                        // 1. Get raw values
                        Long total = doc.getLong("totalMinutes");
                        Long today = doc.getLong("todayMinutes");
                        Long streak = doc.getLong("currentStreak");

                        long totalVal = (total != null) ? total : 0;
                        long todayVal = (today != null) ? today : 0;
                        long streakVal = (streak != null) ? streak : 0;

                        // 2. Format "Total Time" (Minutes -> Days -> Years)
                        String formattedTotal;
                        if (totalVal < 1440) {
                            // Less than 24 hours -> Show Minutes
                            formattedTotal = totalVal + " m";
                        } else if (totalVal < 525600) {
                            // Less than 1 year -> Show Days (1440 mins = 1 day)
                            long days = totalVal / 1440;
                            formattedTotal = days + " d";
                        } else {
                            // More than 1 year -> Show Years (525600 mins = 1 year)
                            long years = totalVal / 525600;
                            formattedTotal = years + " y";
                        }

                        // 3. Set Text
                        tvTotalMinutes.setText(formattedTotal);

                        // Today's minutes always stays as minutes (since it resets every 24h)
                        tvTodayMinutes.setText(String.valueOf(todayVal));

                        // 4. Update Streak Title
                        tvStreakTitle.setText(streakVal + " Day Streak");
                    }
                });
    }
}