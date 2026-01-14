package com.example.knowly;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private RecyclerView searchRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search);
        NavigationHelper.setupNavigation(this);

        // 1. Initialize Firebase and UI
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");
        searchEditText = findViewById(R.id.editTextText);
        searchRecyclerView = findViewById(R.id.recyclerView); // Ensure ID matches XML

        postList = new ArrayList<>();
        // Assuming you have a PostAdapter that takes (Context, List<Post>)
        postAdapter = new PostAdapter(this, postList);

        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchRecyclerView.setAdapter(postAdapter);

        // 2. Add search logic
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchPosts(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchPosts(String queryText) {
        // Firebase Realtime DB filtering is prefix-based
        Query firebaseQuery = mDatabase.orderByChild("content")
                .startAt(queryText)
                .endAt(queryText + "\uf8ff");

        firebaseQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);
                    if (post != null) {
                        postList.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}