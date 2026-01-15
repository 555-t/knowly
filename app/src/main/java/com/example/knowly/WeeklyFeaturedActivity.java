package com.example.knowly;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeeklyFeaturedActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> featuredPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure this layout contains a RecyclerView with id rvWeeklyFeatured
        setContentView(R.layout.fragment_weeklyfeatured);

        // 1. Navigation Setup (Bottom nav/Drawer)
        NavigationHelper.setupNavigation(this);

        // 2. Initialize RecyclerView
        recyclerView = findViewById(R.id.rvWeeklyFeatured);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        featuredPosts = new ArrayList<>();
        postAdapter = new PostAdapter(featuredPosts, this);
        recyclerView.setAdapter(postAdapter);

        // 3. Load the data
        fetchWeeklyTopPosts();
    }

    private void fetchWeeklyTopPosts() {
        // Calculate timestamp for 7 days ago (in milliseconds)
        long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);

        // Query: Get all posts created in the last 7 days from Realtime Database
        Query weeklyQuery = FirebaseDatabase.getInstance().getReference("Posts")
                .orderByChild("timestamp")
                .startAt(sevenDaysAgo);

        weeklyQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                featuredPosts.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);
                    if (post != null) {
                        post.setPostId(ds.getKey());
                        featuredPosts.add(post);
                    }
                }

                // 4. IMPROVED SORTING LOGIC: Sort by Net Score (Upvotes - Downvotes)
                // This prevents posts with high downvotes from outranking clean posts
                Collections.sort(featuredPosts, (p1, p2) -> {
                    int score1 = p1.getUpvote_num() - p1.getDownvote_num();
                    int score2 = p2.getUpvote_num() - p2.getDownvote_num();

                    // Descending order: compare score2 to score1
                    return Integer.compare(score2, score1);
                });

                postAdapter.notifyDataSetChanged();

                if (featuredPosts.isEmpty()) {
                    Toast.makeText(WeeklyFeaturedActivity.this, "No top posts yet this week!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WeeklyFeaturedActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}