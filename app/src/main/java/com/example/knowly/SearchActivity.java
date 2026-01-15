package com.example.knowly;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> allPostsList;
    private EditText searchInput;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search);

        NavigationHelper.setupNavigation(this);
        db = FirebaseFirestore.getInstance();

        searchInput = findViewById(R.id.editTextText);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allPostsList = new ArrayList<>();

        // Initialize with empty list
        postAdapter = new PostAdapter(new ArrayList<Post>(), this);
        recyclerView.setAdapter(postAdapter);

        // 4. Load Data from FIRESTORE
        fetchPostsFromFirestore();

        // 5. Add Search Listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().trim();
                if (searchText.isEmpty()) {
                    postAdapter.updateList(new ArrayList<Post>());
                } else {
                    filter(searchText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchPostsFromFirestore() {
        // Path: posts collection
        db.collection("posts").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                allPostsList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Post post = doc.toObject(Post.class);
                    post.setPostId(doc.getId()); // Essential for click listeners/votes
                    allPostsList.add(post);
                }
            }
        });
    }

    private void filter(String text) {
        List<Post> startsWithList = new ArrayList<>();
        List<Post> containsList = new ArrayList<>();
        String query = text.toLowerCase();

        for (Post post : allPostsList) {
            if (post.getContent() != null) {
                String content = post.getContent().toLowerCase();
                if (content.startsWith(query)) {
                    startsWithList.add(post);
                } else if (content.contains(query)) {
                    containsList.add(post);
                }
            }
        }

        List<Post> finalSortedList = new ArrayList<>();
        finalSortedList.addAll(startsWithList);
        finalSortedList.addAll(containsList);

        postAdapter.updateList(finalSortedList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationHelper.updateNavAvatar(this);
    }
}