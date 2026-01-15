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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StreaksFragment extends Fragment {

    private TextView tvTotalMinutes, tvTodayMinutes;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // MUST inflate fragment_streaks
        View view = inflater.inflate(R.layout.fragment_streaks, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // These IDs must match fragment_streaks.xml
        tvTotalMinutes = view.findViewById(R.id.tvTotalMinutes);
        tvTodayMinutes = view.findViewById(R.id.tvTodayMinutes);

        loadStreakData();

        return view;
    }

    private void loadStreakData() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("users").document(uid)
                .addSnapshotListener((doc, error) -> {
                    if (doc != null && doc.exists()) {
                        Long total = doc.getLong("totalMinutes");
                        Long today = doc.getLong("todayMinutes");

                        tvTotalMinutes.setText(total != null ? String.valueOf(total) : "0");
                        tvTodayMinutes.setText(today != null ? String.valueOf(today) : "0");
                    }
                });
    }
}