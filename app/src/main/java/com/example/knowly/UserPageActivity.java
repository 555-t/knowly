package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserPageActivity extends AppCompatActivity {
    // Add these variables at the top
    private RecyclerView rvUserPosts;
    private PostAdapter postAdapter; // Or whatever your adapter name is
    private List<Post> userPostsList; // Your post model list
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
    private TextView tvBio, tvCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userpage);

        userPostsList = new ArrayList<>();
        // Initialize adapter immediately
        postAdapter = new PostAdapter(userPostsList);
        rvUserPosts.setAdapter(postAdapter);

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
        rvUserPosts = findViewById(R.id.rvUserPosts);
        rvUserPosts.setLayoutManager(new LinearLayoutManager(this));
        userPostsList = new ArrayList<>();
        // --- NEW: Find the ID you added to the XML ---
        tvAvatarText = findViewById(R.id.tvAvatarText);

        // Menu items
        menuLogout = findViewById(R.id.menu_logout);
        menuDelete = findViewById(R.id.menu_delete);

        tvBio = findViewById(R.id.tvBio);
        tvCredentials = findViewById(R.id.tvCredentials);

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
        fetchUserPosts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
        fetchUserPosts();
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
                            String bio = documentSnapshot.getString("bio");
                            String credentials = documentSnapshot.getString("credentials");

                            // Update Username & Avatar
                            if (username != null) {
                                tvName.setText(username);
                                tvAvatarText.setText(String.valueOf(username.charAt(0)).toUpperCase());
                            }

                            // Update Credentials
                            if (tvCredentials != null) {
                                tvCredentials.setText(credentials != null ? credentials : "No credentials added");
                            }

                            // Update Bio
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

    // 1. Add this method to fetch posts
    private void fetchUserPosts() {
        String currentUid = mAuth.getCurrentUser().getUid();

        // Point to your Realtime Database "Posts" node
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Posts")
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        userPostsList.clear();
                        for (com.google.firebase.database.DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Post post = postSnapshot.getValue(Post.class);

                            // ONLY add the post if the author ID matches the current user
                            if (post != null && currentUid.equals(post.getAuthor())) {
                                userPostsList.add(post);
                            }
                        }

                        // Update the adapter
                        if (postAdapter == null) {
                            postAdapter = new PostAdapter(userPostsList);
                            rvUserPosts.setAdapter(postAdapter);
                        } else {
                            postAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                        Toast.makeText(UserPageActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}



