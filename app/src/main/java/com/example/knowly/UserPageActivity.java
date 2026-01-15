package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserPageActivity extends AppCompatActivity {

    private MaterialCardView btnMenuContainer;
    private CardView logoutMenu;
    private CardView btnEditProfile;

    // Text Views
    private TextView tvName, tvEmail, tvFollowers, tvAvatarText, tvBio, tvCredentials;
    private TextView menuLogout, menuDelete;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userpage);

        // 1. INITIALIZE FIREBASE
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. FIND VIEWS
        btnMenuContainer = findViewById(R.id.btnMenuContainer);
        logoutMenu = findViewById(R.id.logoutMenu);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvFollowers = findViewById(R.id.tvFollowers);
        tvAvatarText = findViewById(R.id.tvAvatarText);
        tvBio = findViewById(R.id.tvBio);
        tvCredentials = findViewById(R.id.tvCredentials);

        menuLogout = findViewById(R.id.menu_logout);
        menuDelete = findViewById(R.id.menu_delete);

        // 3. SETUP TABS AND VIEW PAGER
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

        // 4. NAVIGATION & UI SETUP
        NavigationHelper.setupNavigation(this);
        btnMenuContainer.bringToFront();

        // 5. CLICK LISTENERS
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(UserPageActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnMenuContainer.setOnClickListener(v -> toggleMenu());

        // Click outside to close menu
        View root = findViewById(android.R.id.content);
        root.setOnClickListener(v -> logoutMenu.setVisibility(View.GONE));

        // LOGOUT BUTTON
        menuLogout.setOnClickListener(v -> {
            logoutMenu.setVisibility(View.GONE);
            mAuth.signOut();
            Intent intent = new Intent(UserPageActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // DELETE BUTTON
        menuDelete.setOnClickListener(v -> {
            logoutMenu.setVisibility(View.GONE);
            showDeleteConfirmationDialog();
        });

        // 6. LOAD DATA
        loadUserProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Set email immediately from Auth
            if (tvEmail != null) tvEmail.setText(user.getEmail());

            // Fetch profile details from Firestore
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String bio = documentSnapshot.getString("bio");
                            String credentials = documentSnapshot.getString("credentials");

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