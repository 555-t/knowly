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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BookmarksFragment extends Fragment {

    private RecyclerView recyclerView;
    private View layoutEmptyState;
    private PostAdapter adapter;
    private List<Post> bookmarkedPosts = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate the XML you shared
        View view = inflater.inflate(R.layout.fragment_bookmarks, container, false);

        // 2. Initialize Firebase and Views
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.rvBookmarks);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);

        // 3. Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Note: Using requireContext() to satisfy your PostAdapter's Context requirement
        //adapter = new PostAdapter(bookmarkedPosts, requireContext());
        recyclerView.setAdapter(adapter);

        loadBookmarks();

        return view;
    }

    private void loadBookmarks() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        // Listen to the user's document for the "bookmarks" array
        db.collection("users").document(uid)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<String> bookmarkIds = (List<String>) documentSnapshot.get("bookmarks");

                        if (bookmarkIds == null || bookmarkIds.isEmpty()) {
                            updateUI(true); // Show empty state
                        } else {
                            fetchBookmarkedPosts(bookmarkIds);
                        }
                    }
                });
    }

    private void fetchBookmarkedPosts(List<String> ids) {
        // Firestore query to get only posts whose IDs are in the bookmarks list
        db.collection("posts")
                .whereIn(FieldPath.documentId(), ids)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookmarkedPosts.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            bookmarkedPosts.add(post);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateUI(bookmarkedPosts.isEmpty());
                });
    }

    private void updateUI(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }
}