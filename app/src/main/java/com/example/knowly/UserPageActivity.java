package com.example.knowly;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserPageActivity extends AppCompatActivity {

    private MaterialCardView btnMenuContainer;
    private CardView logoutMenu, btnEditProfile;
    private TextView tvName, tvEmail, tvAvatarText, tvBio, tvCredentials;
    private TextView tvPostsCount, tvFollowersCount, tvFollowingCount;
    private TextView menuLogout, menuDelete;
    private ChipGroup cgInterests;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userpage);

        // 1. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

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

        // 3. Setup TabLayout & ViewPager2
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

        // 4. Navigation & Menu Setup
        NavigationHelper.setupNavigation(this);

        // 5. Click Listeners
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        btnMenuContainer.setOnClickListener(v -> toggleMenu());

        // Click outside to close menu
        View root = findViewById(android.R.id.content);
        root.setOnClickListener(v -> logoutMenu.setVisibility(View.GONE));

        // LOGOUT BUTTON
        menuLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // DELETE BUTTON
        menuDelete.setOnClickListener(v -> {
            Toast.makeText(this, "Account deletion requires re-authentication.", Toast.LENGTH_SHORT).show();
            logoutMenu.setVisibility(View.GONE);
            showDeleteConfirmationDialog();
        });

        // 6. Load Initial Data
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Set email immediately from Auth
            if (tvEmail != null) tvEmail.setText(user.getEmail());
        String uid = mAuth.getUid();
        if (uid == null) return;

        // 1. Fetch User Data & Follow Stats
        mDatabase.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !isFinishing()) {
                    String username = snapshot.child("username").getValue(String.class);
                    tvName.setText(username != null ? username : "User");
                    tvEmail.setText(snapshot.child("email").getValue(String.class));

                    // FOLLOWERS/FOLLOWING:
                    // Counts the number of entries under the "followers" and "following" nodes
                    long followers = snapshot.child("followers").getChildrenCount();
                    long following = snapshot.child("following").getChildrenCount();

                    tvFollowersCount.setText(followers + " Followers");
                    tvFollowingCount.setText(following + " Following");
            // Fetch profile details from Firestore
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String bio = documentSnapshot.getString("bio");
                            String credentials = documentSnapshot.getString("credentials");

                    if (username != null && !username.isEmpty()) {
                        tvAvatarText.setText(username.substring(0, 1).toUpperCase());
                    }
                    displayInterests(snapshot.child("interests"));

                    // 2. Fetch Post Count (Triggered here so we have the 'username' to match)
                    updatePostCount(uid, username);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updatePostCount(String currentUid, String currentUsername) {
        mDatabase.child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot postSnap : snapshot.getChildren()) {
                    // Your DB uses 'author' for username and 'publisher' or 'uid' for IDs
                    String authorField = postSnap.child("author").getValue(String.class);
                    String publisherField = postSnap.child("publisher").getValue(String.class);

                    // Match by either Username OR UID to be safe
                    if (currentUid.equals(publisherField) ||
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

    private void displayInterests(DataSnapshot interestsSnapshot) {
        cgInterests.removeAllViews();
        if (!interestsSnapshot.exists()) return;

        for (DataSnapshot ds : interestsSnapshot.getChildren()) {
            String interest = ds.getValue(String.class);
            if (interest != null) {
                Chip chip = new Chip(this);
                chip.setText(interest);
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
                chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
                chip.setChipStrokeWidth(2f);
                chip.setTextColor(Color.parseColor("#424242"));

                try {
                    chip.setTypeface(ResourcesCompat.getFont(this, R.font.inter_medium));
                } catch (Exception e) {
                    // Fallback to system medium
                    chip.setTypeface(null, android.graphics.Typeface.BOLD);
                }

                chip.setClickable(false);
                chip.setCheckable(false);
                cgInterests.addView(chip);
            }
                            if (username != null && !username.isEmpty()) {
                                tvName.setText(username);
                                // Update the avatar circle with the first letter
                                if (tvAvatarText != null) {
                                    tvAvatarText.setText(String.valueOf(username.charAt(0)).toUpperCase());
                                }
                            }

                            if (tvCredentials != null) {
                                tvCredentials.setText(credentials != null ? credentials : "No credentials added");
                            }

                            if (tvBio != null) {
                                tvBio.setText(bio != null ? bio : "Tell us about yourself...");
                            }
                        }
                    });
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

    // --- DELETE ACCOUNT LOGIC ---

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> performDeleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDeleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            // 1. Delete User Data from Firestore first
            db.collection("users").document(uid)
                    .delete()
                    .addOnSuccessListener(aVoid -> {

                        // 2. Delete the User from Authentication
                        user.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(UserPageActivity.this, "Account Deleted", Toast.LENGTH_SHORT).show();

                                        // 3. Redirect to Login
                                        Intent intent = new Intent(UserPageActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // This happens if the login session is too old (Firebase Security Rule)
                                        Toast.makeText(UserPageActivity.this, "Security Requirement: Please Log Out and Log In again to delete.", Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UserPageActivity.this, "Error deleting data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}