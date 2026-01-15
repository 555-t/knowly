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
    private List<Post> allPostsList; // Keeps a copy of ALL posts hidden in background
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search);

        // 1. Setup Navigation
        NavigationHelper.setupNavigation(this);

        // 2. Initialize Views
        searchInput = findViewById(R.id.editTextText);
        recyclerView = findViewById(R.id.recyclerView);

        // 3. Setup Recycler View
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allPostsList = new ArrayList<>();

        // Initialize adapter with empty list (Screen starts empty)
        // Pass 'this' as the second argument
        postAdapter = new PostAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(postAdapter);

        // 4. Load Data (But don't show it yet!)
        fetchPostsFromRealtimeDB();

        // 5. Add Search Listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().trim();

                // LOGIC CHANGE:
                if (searchText.isEmpty()) {
                    // If text is empty, clear the list
                    postAdapter.updateList(new ArrayList<>());
                } else {
                    // If text exists, filter and show results
                    filter(searchText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchPostsFromRealtimeDB() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allPostsList.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        post.setPostId(postSnapshot.getKey());
                        allPostsList.add(post);
                    }
                }
                // NOTE: We do NOT call updateList() here anymore.
                // The data is ready in 'allPostsList', but we wait for the user to type.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filter(String text) {
        // 1. Create two separate lists
        List<Post> startsWithList = new ArrayList<>();
        List<Post> containsList = new ArrayList<>();

        String query = text.toLowerCase();

        for (Post post : allPostsList) {
            if (post.getContent() != null) {
                String content = post.getContent().toLowerCase();

                // 2. Sort them into buckets
                if (content.startsWith(query)) {
                    // Priority 1: The post actually starts with "hel" (e.g. "Hello")
                    startsWithList.add(post);
                } else if (content.contains(query)) {
                    // Priority 2: The word is hidden inside (e.g. "I have hella money")
                    containsList.add(post);
                }
            }
        }

        // 3. Combine them: StartsWith first, Contains second
        List<Post> finalSortedList = new ArrayList<>();
        finalSortedList.addAll(startsWithList);
        finalSortedList.addAll(containsList);

        // 4. Update Adapter
        postAdapter.updateList(finalSortedList);
    }
}