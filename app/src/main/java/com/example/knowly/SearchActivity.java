package com.example.knowly;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Realtime Database Imports
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> allPostsList; // Keeps a copy of ALL posts
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search);

        // 1. Setup Navigation (Bottom Bar)
        NavigationHelper.setupNavigation(this);

        // 2. Initialize Views
        searchInput = findViewById(R.id.editTextText);
        recyclerView = findViewById(R.id.recyclerView);

        // 3. Setup Recycler View
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allPostsList = new ArrayList<>();

        // Initialize adapter with empty list
        postAdapter = new PostAdapter(new ArrayList<>());
        recyclerView.setAdapter(postAdapter);

        // 4. Load Data from Realtime Database
        fetchPostsFromRealtimeDB();

        // 5. Add Search Listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter immediately when user types
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchPostsFromRealtimeDB() {
        // Matches the "Posts" reference used in your PostAdapter
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allPostsList.clear(); // Clear old data to avoid duplicates

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    // Convert JSON data to Post object
                    Post post = postSnapshot.getValue(Post.class);

                    if (post != null) {
                        // Ensure the ID is set (sometimes it's missing in the body)
                        post.setPostId(postSnapshot.getKey());
                        allPostsList.add(post);
                    }
                }

                // Show the full list initially
                postAdapter.updateList(allPostsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filter(String text) {
        List<Post> filteredList = new ArrayList<>();

        // Loop through the downloaded list
        for (Post post : allPostsList) {
            if (post.getContent() != null) {
                // Check if the content contains the search text (Case Insensitive)
                if (post.getContent().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(post);
                }
            }
        }

        // Pass filtered list to adapter
        postAdapter.updateList(filteredList);
    }
}