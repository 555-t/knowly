package com.example.knowly;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserPageActivity extends AppCompatActivity {

    private MaterialCardView btnMenuContainer;
    private CardView logoutMenu, btnEditProfile;
    private TextView tvName, tvEmail, tvAvatarText, tvBio, tvCredentials;
    private TextView tvPostsCount, tvFollowersCount, tvFollowingCount;
    private TextView menuLogout, menuDelete;
    private ChipGroup cgInterests;

    private FirebaseAuth mAuth;
    private DatabaseReference mRealtimeDb; // For Posts
    private FirebaseFirestore mFirestore;  // For Profile & Follows

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userpage);

        // 1. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mRealtimeDb = FirebaseDatabase.getInstance().getReference();
        mFirestore = FirebaseFirestore.getInstance();

        // 2. Initialize Views
        btnMenuContainer = findViewById(R.id.btnMenuContainer);
        logoutMenu = findViewById(R.id.logoutMenu);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvAvatarText = findViewById(R.id.tvAvatarText);
        tvBio = findViewById(R.id.tvBio);
        tvCredentials = findViewById(R.id.tvCredentials);

        tvPostsCount = findViewById(R.id.tvPostsCount);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);

        cgInterests = findViewById(R.id.cgInterests);
        menuLogout = findViewById(R.id.menu_logout);
        menuDelete = findViewById(R.id.menu_delete);

        // 3. Setup Tabs
        TabLayout tabLayout = findViewById(R.id.profileTabs);
        ViewPager2 viewPager = findViewById(R.id.profileViewPager);
        ProfilePagerAdapter pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("My Posts"); break;
                case 1: tab.setText("Bookmarks"); break;
                case 2: tab.setText("Streaks"); break;
            }
        }).attach();

        // 4. Navigation & Listeners
        NavigationHelper.setupNavigation(this);

        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        btnMenuContainer.setOnClickListener(v -> toggleMenu());

        menuLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        menuDelete.setOnClickListener(v -> {
            logoutMenu.setVisibility(View.GONE);
            showDeleteConfirmationDialog();
        });

        // 5. Load Data
        loadUserProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        tvEmail.setText(user.getEmail());

        // --- FETCH PROFILE ---
        mFirestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateUIWithFirestoreData(documentSnapshot, uid);
                    } else {
                        tvName.setText("User");
                        tvAvatarText.setText("U");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUIWithFirestoreData(DocumentSnapshot doc, String uid) {
        // 1. Username & Avatar
        String username = doc.getString("username");
        if (username != null && !username.isEmpty()) {
            tvName.setText(username);
            tvAvatarText.setText(username.substring(0, 1).toUpperCase());
            updatePostCount(uid, username);
        } else {
            tvName.setText("User");
            tvAvatarText.setText("U");
            updatePostCount(uid, null);
        }

        // 2. Bio & Credentials
        String bio = doc.getString("bio");
        String credentials = doc.getString("credentials");

        tvBio.setText((bio != null && !bio.isEmpty()) ? bio : "No bio yet...");
        tvCredentials.setText((credentials != null && !credentials.isEmpty()) ? credentials : "Student");

        // 3. Interests
        List<String> interests = (List<String>) doc.get("interests");
        displayInterests(interests);

        // 4. Update Follow Counts (NEW)
        updateFollowCounts(uid);
    }

    // --- NEW: FETCH FOLLOWER COUNTS ---
    private void updateFollowCounts(String uid) {
        // Count 'followers' subcollection
        mFirestore.collection("users").document(uid).collection("followers")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    if (snapshots != null) {
                        tvFollowersCount.setText(snapshots.size() + " Followers");
                    } else {
                        tvFollowersCount.setText("0 Followers");
                    }
                });

        // Count 'following' subcollection
        mFirestore.collection("users").document(uid).collection("following")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    if (snapshots != null) {
                        tvFollowingCount.setText(snapshots.size() + " Following");
                    } else {
                        tvFollowingCount.setText("0 Following");
                    }
                });
    }

    // --- REALTIME DB FOR POSTS ---
    private void updatePostCount(String currentUid, String currentUsername) {
        mRealtimeDb.child("Posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot postSnap : snapshot.getChildren()) {
                    String authorField = postSnap.child("author").getValue(String.class);
                    // Match by UID (safest) or Username
                    if (currentUid.equals(authorField) ||
                            (currentUsername != null && currentUsername.equals(authorField))) {
                        count++;
                    }
                }
                tvPostsCount.setText(count + " Posts");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayInterests(List<String> interests) {
        cgInterests.removeAllViews();
        if (interests == null || interests.isEmpty()) return;

        for (String interest : interests) {
            Chip chip = new Chip(this);
            chip.setText(interest);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
            chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
            chip.setChipStrokeWidth(2f);
            chip.setTextColor(Color.parseColor("#424242"));
            try {
                chip.setTypeface(ResourcesCompat.getFont(this, R.font.inter_medium));
            } catch (Exception e) {
                chip.setTypeface(null, android.graphics.Typeface.BOLD);
            }
            chip.setClickable(false);
            chip.setCheckable(false);
            cgInterests.addView(chip);
        }
    }

    private void toggleMenu() {
        if (logoutMenu.getVisibility() == View.VISIBLE) {
            logoutMenu.setVisibility(View.GONE);
        } else {
            logoutMenu.setVisibility(View.VISIBLE);
            logoutMenu.bringToFront();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Delete", (dialog, which) -> performDeleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDeleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            mFirestore.collection("users").document(uid).delete().addOnSuccessListener(aVoid -> {
                user.delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(UserPageActivity.this, LoginActivity.class));
                        finish();
                    }
                });
            });
        }
    }
}