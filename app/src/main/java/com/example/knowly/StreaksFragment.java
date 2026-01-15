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

    private TextView tvTotalMinutes, tvTodayMinutes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_streaks, container, false);

        // Match these IDs to your XML (see step 2 below)
        tvTotalMinutes = view.findViewById(R.id.tvTotaslMinutes);
        tvTodayMinutes = view.findViewById(R.id.tvTodayMinutes);

        loadStreakData();
        return view;
    }

    private void loadStreakData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("users").document(uid)
                .addSnapshotListener((doc, error) -> {
                    if (doc != null && doc.exists()) {
                        // Fetching numbers from Firestore
                        Long total = doc.getLong("totalMinutes");
                        Long today = doc.getLong("todayMinutes");

                        tvTotalMinutes.setText(total != null ? String.valueOf(total) : "0");
                        tvTodayMinutes.setText(today != null ? String.valueOf(today) : "0");
                    }
                });
    }
}