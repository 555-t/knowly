package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        // 1. Initialize Firebase Reference
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");

        // 2. Setup RecyclerView (The Feed)
        recyclerView = findViewById(R.id.recyclerViewPosts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        recyclerView.setAdapter(postAdapter);

        // 3. Setup Navigation: Create Post Button
        MaterialCardView createPostBtn = findViewById(R.id.createpostbutton);
        createPostBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, CreatePostActivity.class);
            startActivity(intent);
        });

        // 4. Setup Navigation: Weekly Featured (Bottom Nav)
        ImageView navWeekly = findViewById(R.id.navWeekly);
        navWeekly.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, WeeklyFeaturedActivity.class);
            startActivity(intent);
        });

        // 5. Load Posts from Firebase
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
                        // Adds newest post to the top of the list
                        postList.add(0, post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePage.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}