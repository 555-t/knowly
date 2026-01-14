package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserPageActivity extends AppCompatActivity {

    private MaterialCardView btnMenuContainer;
    private CardView logoutMenu;
    private CardView btnEditProfile;

    // Text Views
    private TextView tvName, tvEmail, tvFollowers;
    private TextView tvAvatarText; // <--- NEW: For the initial letter

    private TextView menuLogout, menuDelete;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userpage);

        // 1. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bottom nav setup
        NavigationHelper.setupNavigation(this);

        // 2. Initialize Views
        btnMenuContainer = findViewById(R.id.btnMenuContainer);
        logoutMenu = findViewById(R.id.logoutMenu);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        // Text Views
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail); // Note: If you removed this from XML, remove this line
        tvFollowers = findViewById(R.id.tvFollowers);

        // --- NEW: Find the ID you added to the XML ---
        tvAvatarText = findViewById(R.id.tvAvatarText);

        // Menu items
        menuLogout = findViewById(R.id.menu_logout);
        menuDelete = findViewById(R.id.menu_delete);

        // Force menu button above other views
        btnMenuContainer.bringToFront();

        // Edit Profile Click Listener
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(UserPageActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnMenuContainer.setOnClickListener(v -> toggleMenu());

        // Click outside to close
        View root = findViewById(android.R.id.content);
        root.setOnClickListener(v -> logoutMenu.setVisibility(View.GONE));

        menuLogout.setOnClickListener(v -> {
            logoutMenu.setVisibility(View.GONE);
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(UserPageActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        menuDelete.setOnClickListener(v -> {
            logoutMenu.setVisibility(View.GONE);
            Toast.makeText(this, "Delete Account clicked", Toast.LENGTH_SHORT).show();
        });

        // 3. Load Data immediately
        loadUserProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();

        // 1. Find the NAV bar text view
        // Since the layout is <include>d, we can find it directly by ID
        TextView tvNavAvatarText = findViewById(R.id.tvNavAvatarText);

        if (user != null) {
            if (tvEmail != null) {
                tvEmail.setText(user.getEmail());
            }

            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");

                            if (username != null && !username.isEmpty()) {
                                tvName.setText(username);

                                // --- CALCULATE INITIAL ---
                                String initial = String.valueOf(username.charAt(0)).toUpperCase();

                                // 2. Update MAIN Profile Avatar
                                if (tvAvatarText != null) {
                                    tvAvatarText.setText(initial);
                                }

                                // 3. Update NAVIGATION Avatar
                                if (tvNavAvatarText != null) {
                                    tvNavAvatarText.setText(initial);
                                }

                            } else {
                                tvName.setText("User");
                                if (tvAvatarText != null) tvAvatarText.setText("U");
                                if (tvNavAvatarText != null) tvNavAvatarText.setText("U");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
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
}



