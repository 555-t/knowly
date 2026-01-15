package com.example.knowly;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity {

    // --- Feed / Firebase ---
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private DatabaseReference mDatabase;

    // --- Tabs (For You / Following) ---
    private CardView cardForYou, cardFollowing;
    private TextView btnForYou, btnFollowing;

    // --- Filtering Data ---
    private List<String> currentUserInterests = new ArrayList<>();
    private List<String> currentUserFollowing = new ArrayList<>();
    private String currentUserId;

    private boolean isForYouSelected = true; // Default to For You

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        // 1. Initialize Views and Navigation
        NavigationHelper.setupNavigation(this);
        cardForYou = findViewById(R.id.cardForYou);
        cardFollowing = findViewById(R.id.cardFollowing);
        btnForYou = findViewById(R.id.btnForYou);
        btnFollowing = findViewById(R.id.btnFollowing);

        // 2. Setup RecyclerView
        recyclerView = findViewById(R.id.rvFeed);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        recyclerView.setAdapter(postAdapter);

        // 3. Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");
        currentUserId = FirebaseAuth.getInstance().getUid();

        // 4. Load Data
        loadUserPreferences();
        fetchPostsFromFirebase();

        // 5. Tab Click Listeners
        cardForYou.setOnClickListener(v -> setSelectedTab(true));
        cardFollowing.setOnClickListener(v -> setSelectedTab(false));

        // 6. Navigation Buttons
        MaterialCardView createPostBtn = findViewById(R.id.createpostbutton);
        if (createPostBtn != null) {
            createPostBtn.setOnClickListener(v -> {
                startActivity(new Intent(HomePage.this, CreatePostActivity.class));
            });
        }

        ImageView navWeekly = findViewById(R.id.navWeekly);
        if (navWeekly != null) {
            navWeekly.setOnClickListener(v -> {
                startActivity(new Intent(HomePage.this, WeeklyFeaturedActivity.class));
            });
        }
    }

    private void loadUserPreferences() {
        if (currentUserId == null) return;

        // --- SYNC WITH FIRESTORE (Following List) ---
        // This listens to the same place your Unfollow button deletes from
        FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                .collection("following")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) return;

                    if (snapshot != null) {
                        currentUserFollowing.clear();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            // The document ID is the UID of the user you follow
                            currentUserFollowing.add(doc.getId());
                        }
                        // Refresh the feed immediately when follow list changes
                        fetchPostsFromFirebase();
                    }
                });

        // --- SYNC WITH REALTIME DB (Interests) ---
        FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("interests")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentUserInterests.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                currentUserInterests.add(ds.getValue(String.class));
                            }
                        }
                        fetchPostsFromFirebase();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setSelectedTab(boolean isForYou) {
        isForYouSelected = isForYou;

        if (isForYouSelected) {
            btnForYou.setBackgroundResource(R.drawable.gradient_button);
            btnForYou.setTextColor(Color.WHITE);
            btnFollowing.setBackgroundResource(R.drawable.bg_tab_flat);
            btnFollowing.setTextColor(Color.parseColor("#7A7A7A"));
        } else {
            btnFollowing.setBackgroundResource(R.drawable.gradient_button);
            btnFollowing.setTextColor(Color.WHITE);
            btnForYou.setBackgroundResource(R.drawable.bg_tab_flat);
            btnForYou.setTextColor(Color.parseColor("#7A7A7A"));
        }

        fetchPostsFromFirebase();
    }

    private void fetchPostsFromFirebase() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        post.setPostId(dataSnapshot.getKey());

                        if (isForYouSelected) {
                            // FOR YOU: Show posts matching interests OR all if none selected
                            if (currentUserInterests.isEmpty()) {
                                postList.add(0, post);
                            } else if (post.getCategories() != null) {
                                for (String cat : post.getCategories()) {
                                    if (currentUserInterests.contains(cat)) {
                                        postList.add(0, post);
                                        break;
                                    }
                                }
                            }
                        } else {
                            // FOLLOWING: ONLY show posts if author is in the currentUserFollowing list
                            if (currentUserFollowing.contains(post.getAuthor())) {
                                postList.add(0, post);
                            }
                        }
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePage.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}