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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserPageActivity extends AppCompatActivity {

    private RecyclerView rvUserPosts;
    private PostAdapter postAdapter;
    private List<Post> userPostsList;
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

        // 1. INITIALIZE FIREBASE FIRST
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. FIND ALL VIEWS (Crucial: Do this before using them)
        rvUserPosts = findViewById(R.id.rvUserPosts);
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

        // 3. SETUP RECYCLERVIEW
        userPostsList = new ArrayList<>();
        postAdapter = new PostAdapter(userPostsList);
        rvUserPosts.setLayoutManager(new LinearLayoutManager(this));
        rvUserPosts.setAdapter(postAdapter);

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

        menuLogout.setOnClickListener(v -> {
            logoutMenu.setVisibility(View.GONE);
            mAuth.signOut();
            Intent intent = new Intent(UserPageActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        menuDelete.setOnClickListener(v -> {
            logoutMenu.setVisibility(View.GONE);
            Toast.makeText(this, "Delete Account clicked", Toast.LENGTH_SHORT).show();
        });

        // 6. LOAD DATA
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

                            if (username != null && !username.isEmpty()) {
                                tvName.setText(username);
                                tvAvatarText.setText(String.valueOf(username.charAt(0)).toUpperCase());
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

    private void fetchUserPosts() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return; // Safety check to prevent crash

        String currentUid = user.getUid();

        FirebaseDatabase.getInstance().getReference("Posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userPostsList.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Post post = postSnapshot.getValue(Post.class);
                            if (post != null && currentUid.equals(post.getAuthor())) {
                                userPostsList.add(post);
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserPageActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}