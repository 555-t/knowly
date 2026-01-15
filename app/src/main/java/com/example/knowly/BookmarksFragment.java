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
import com.google.firebase.database.*;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class BookmarksFragment extends Fragment {

    private RecyclerView recyclerView;
    private View layoutEmptyState; // Added to control the empty screen
    private PostAdapter adapter;
    private List<Post> bookmarkedPosts = new ArrayList<>();
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmarks, container, false);

        mAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.rvBookmarks);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState); // Initialize this

        if (getActivity() != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new PostAdapter(bookmarkedPosts, requireContext());
            recyclerView.setAdapter(adapter);
            loadBookmarkedIds();
        }
        return view;
    }

    private void loadBookmarkedIds() {
        if (mAuth.getCurrentUser() == null) return;

        FirebaseFirestore.getInstance().collection("users").document(mAuth.getUid())
                .addSnapshotListener((snapshot, e) -> {
                    if (isAdded() && snapshot != null && snapshot.exists()) {
                        List<String> ids = (List<String>) snapshot.get("bookmarks");
                        if (ids != null && !ids.isEmpty()) {
                            fetchPosts(ids);
                        } else {
                            // CASE: List is empty - Show "No bookmarks"
                            bookmarkedPosts.clear();
                            updateVisibility(true);
                            if (adapter != null) adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void fetchPosts(List<String> ids) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        bookmarkedPosts.clear();

        for (String id : ids) {
            postsRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!isAdded()) return;

                    Post post = snapshot.getValue(Post.class);
                    if (post != null) {
                        post.setPostId(snapshot.getKey());

                        boolean alreadyExists = false;
                        for (Post p : bookmarkedPosts) {
                            if (p.getPostId().equals(post.getPostId())) {
                                alreadyExists = true;
                                break;
                            }
                        }

                        if (!alreadyExists) {
                            bookmarkedPosts.add(post);

                            // SUCCESS: Hide empty layout, show RecyclerView
                            updateVisibility(false);

                            if (adapter != null) adapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    // Helper method to switch visibility
    private void updateVisibility(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }
}