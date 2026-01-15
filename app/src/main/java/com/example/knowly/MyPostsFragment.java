package com.example.knowly;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class MyPostsFragment extends Fragment {

    private RecyclerView recyclerView;
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

        // Use requireContext() to ensure a non-null context is passed to the adapter
        adapter = new PostAdapter(postList, requireContext());
        recyclerView.setAdapter(adapter);

        loadUserPosts();

        return view;
    }

    private void loadUserPosts() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        // Use FirebaseDatabase (RTDB) instead of FirebaseFirestore
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);

                    // CRITICAL: Match the "author" field in your RTDB image
                    // If your database stores "student_user" but the UID is something else,
                    // you must ensure these match.
                    if (post != null && post.getAuthor() != null && post.getAuthor().equals(uid)) {
                        post.setPostId(ds.getKey());
                        postList.add(0, post); // Add to top for newest first
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RTDBError", "Query failed: " + error.getMessage());
            }
        });
    }
}