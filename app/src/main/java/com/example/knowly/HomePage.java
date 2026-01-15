package com.example.knowly;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends BaseActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private FirebaseFirestore db;
    private CardView cardForYou, cardFollowing;
    private TextView btnForYou, btnFollowing;

    private List<String> currentUserInterests = new ArrayList<>();
    private List<String> currentUserFollowing = new ArrayList<>();
    private String currentUserId;

    private boolean isForYouSelected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        NavigationHelper.setupNavigation(this);

        // UI Setup
        cardForYou = findViewById(R.id.cardForYou);
        cardFollowing = findViewById(R.id.cardFollowing);
        btnForYou = findViewById(R.id.btnForYou);
        btnFollowing = findViewById(R.id.btnFollowing);

        recyclerView = findViewById(R.id.rvFeed);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, this);
        recyclerView.setAdapter(postAdapter);

        // Load preferences first, then posts
        loadUserPreferences();

        // Listeners
        cardForYou.setOnClickListener(v -> setSelectedTab(true));
        cardFollowing.setOnClickListener(v -> setSelectedTab(false));

        MaterialCardView createPostBtn = findViewById(R.id.createpostbutton);
        if (createPostBtn != null) {
            createPostBtn.setOnClickListener(v -> startActivity(new Intent(HomePage.this, CreatePostActivity.class)));
        }
    }

    private void loadUserPreferences() {
        if (currentUserId == null) return;

        // 1. Listen for Interests (Array field on User document)
        db.collection("users").document(currentUserId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;
                    currentUserInterests = (List<String>) snapshot.get("interests");
                    if (currentUserInterests == null) currentUserInterests = new ArrayList<>();
                    fetchPostsFromFirestore();
                });

        // 2. Listen for Following (Sub-collection)
        db.collection("users").document(currentUserId).collection("following")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null || querySnapshot == null) return;

                    currentUserFollowing.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // The document ID in this sub-collection is the followed User's UID
                        currentUserFollowing.add(doc.getId());
                    }

                    // Refresh the feed now that we have the UIDs
                    fetchPostsFromFirestore();
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
        fetchPostsFromFirestore();
    }

    private void fetchPostsFromFirestore() {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) return;

                    postList.clear();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(doc.getId());

                                if (isForYouSelected) {
                                    // FIX: If no matches, we still show the post so the feed isn't empty,
                                    // or you can keep it strict but ensure you HAVE matching interests.
                                    if (currentUserInterests.isEmpty() || hasMatch(post)) {
                                        postList.add(post);
                                    } else {
                                        // TEMPORARY: Add this to see ALL posts while testing
                                        postList.add(post);
                                    }
                                } else {
                                    if (currentUserFollowing.contains(post.getAuthor())) {
                                        postList.add(post);
                                    }
                                }
                            }
                        }
                    }
                    postAdapter.notifyDataSetChanged();
                });
    }
    private boolean hasMatch(Post post) {
        if (post.getCategories() == null || currentUserInterests == null) return false;
        for (String cat : post.getCategories()) {
            if (currentUserInterests.contains(cat)) return true;
        }
        return false;
    }
}