package com.example.knowly;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class MyPostsFragment extends Fragment {

    private RecyclerView recyclerView;
    // Use your existing PostAdapter
    private PostAdapter adapter;
    private List<Post> postList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_posts, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.rvMyPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize your adapter (ensure you have a Post class and PostAdapter)
        adapter = new PostAdapter(postList, getContext());
        recyclerView.setAdapter(adapter);

        loadUserPosts();

        return view;
    }

    private void loadUserPosts() {
        String uid = mAuth.getCurrentUser().getUid();

        // Query posts where the authorId matches current user
        db.collection("posts")
                .whereEqualTo("authorId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        postList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            Post post = doc.toObject(Post.class);
                            postList.add(post);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}