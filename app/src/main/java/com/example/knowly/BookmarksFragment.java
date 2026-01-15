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
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Added Firestore instance

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmarks, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.rvBookmarks);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);

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

        // Listen to the 'bookmarks' array in the user's Firestore document
        db.collection("users").document(mAuth.getUid())
                .addSnapshotListener((snapshot, e) -> {
                    if (isAdded() && snapshot != null && snapshot.exists()) {
                        List<String> ids = (List<String>) snapshot.get("bookmarks");
                        if (ids != null && !ids.isEmpty()) {
                            fetchPostsFromFirestore(ids);
                        } else {
                            bookmarkedPosts.clear();
                            updateVisibility(true);
                            if (adapter != null) adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void fetchPostsFromFirestore(List<String> ids) {
        // Query the 'posts' collection for documents whose ID is in our bookmark list
        db.collection("posts")
                .whereIn(FieldPath.documentId(), ids)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    bookmarkedPosts.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            post.setPostId(doc.getId());
                            bookmarkedPosts.add(post);
                        }
                    }

                    // Toggle visibility based on whether we found the posts
                    updateVisibility(bookmarkedPosts.isEmpty());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) updateVisibility(true);
                });
    }

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