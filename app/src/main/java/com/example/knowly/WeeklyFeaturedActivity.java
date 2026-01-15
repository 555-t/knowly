package com.example.knowly;

import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeeklyFeaturedActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> featuredPosts;
    private FirebaseFirestore db; // Added Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_weeklyfeatured);

        NavigationHelper.setupNavigation(this);
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.rvWeeklyFeatured);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        featuredPosts = new ArrayList<>();
        postAdapter = new PostAdapter(featuredPosts, this);
        recyclerView.setAdapter(postAdapter);

        fetchWeeklyTopPosts();
    }

    private void fetchWeeklyTopPosts() {
        // 1. Calculate the date 7 days ago
        long sevenDaysAgoMillis = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);

        // 2. CONVERT to Firestore Timestamp for the query
        com.google.firebase.Timestamp timestampFilter = new com.google.firebase.Timestamp(new java.util.Date(sevenDaysAgoMillis));

        // 3. Firestore Query using the Timestamp object
        db.collection("posts")
                .whereGreaterThanOrEqualTo("timestamp", timestampFilter) // Now matching types!
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        featuredPosts.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Post post = doc.toObject(Post.class);
                            post.setPostId(doc.getId());
                            featuredPosts.add(post);
                        }

                        // Score-based sorting (Trending logic)
                        Collections.sort(featuredPosts, (p1, p2) -> {
                            long score1 = p1.getUpvote_num() - p1.getDownvote_num();
                            long score2 = p2.getUpvote_num() - p2.getDownvote_num();
                            return Long.compare(score2, score1); // Descending
                        });

                        postAdapter.notifyDataSetChanged();
                    }
                });
    }}