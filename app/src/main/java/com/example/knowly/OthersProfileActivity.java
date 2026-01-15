package com.example.knowly;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OthersProfileActivity extends AppCompatActivity {

    private String otherUserId, currentUserId;
    private TextView tvName, tvFollowerCount, tvFollowingCount;
    private CardView btnFollow;
    private TextView btnFollowTextView;
    private ImageButton backButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration profileListener;
    private boolean isFollowing = false;

    private RecyclerView rvOtherUserPosts;
    private PostAdapter postAdapter;
    private List<Post> userPostsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view_other);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        otherUserId = getIntent().getStringExtra("USER_ID");

        if (otherUserId == null || currentUserId == null) {
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 1. Link Views
        tvName = findViewById(R.id.other_username_text);
        tvFollowerCount = findViewById(R.id.tvFollowerCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        btnFollow = findViewById(R.id.btnFollow); // The CardView button
        btnFollowTextView = findViewById(R.id.btnFollowTextView); // The text inside
        backButton = findViewById(R.id.backButton_APV);

        // 2. Setup RecyclerView
        rvOtherUserPosts = findViewById(R.id.rvOtherUserPosts);
        userPostsList = new ArrayList<>();
        postAdapter = new PostAdapter(userPostsList, this);
        if (rvOtherUserPosts != null) {
            rvOtherUserPosts.setLayoutManager(new LinearLayoutManager(this));
            rvOtherUserPosts.setAdapter(postAdapter);
        }

        // 3. Listeners
        if (backButton != null) backButton.setOnClickListener(v -> finish());

        checkFollowStatus(); // Initial check to see if we follow them
        btnFollow.setOnClickListener(v -> toggleFollow());

        loadOtherUserData();
        fetchOtherUserPosts();
    }

    private void checkFollowStatus() {
        // Check if current user is in other user's followers sub-collection
        db.collection("users").document(otherUserId)
                .collection("followers").document(currentUserId)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot != null && snapshot.exists()) {
                        isFollowing = true;
                        btnFollowTextView.setText("Unfollow");
                        btnFollow.setCardBackgroundColor(0xFFBDBDBD); // Grey
                    } else {
                        isFollowing = false;
                        btnFollowTextView.setText("Follow");
                        btnFollow.setCardBackgroundColor(0xFF00ACC1); // Original Blue/Green
                    }
                });
    }

    private void toggleFollow() {
        DocumentReference otherUserRef = db.collection("users").document(otherUserId);
        DocumentReference currentUserRef = db.collection("users").document(currentUserId);

        if (isFollowing) {
            // UNFOLLOW LOGIC
            // Remove from sub-collections
            otherUserRef.collection("followers").document(currentUserId).delete();
            currentUserRef.collection("following").document(otherUserId).delete();

            // Decrease counts
            otherUserRef.update("followerCount", FieldValue.increment(-1));
            currentUserRef.update("followingCount", FieldValue.increment(-1));

            Toast.makeText(this, "Unfollowed", Toast.LENGTH_SHORT).show();
        } else {
            // FOLLOW LOGIC
            Map<String, Object> data = new HashMap<>();
            data.put("timestamp", FieldValue.serverTimestamp());

            // Add to sub-collections
            otherUserRef.collection("followers").document(currentUserId).set(data);
            currentUserRef.collection("following").document(otherUserId).set(data);

            // Increase counts
            otherUserRef.update("followerCount", FieldValue.increment(1));
            currentUserRef.update("followingCount", FieldValue.increment(1));

            Toast.makeText(this, "Following", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadOtherUserData() {
        profileListener = db.collection("users").document(otherUserId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("username");
                        Long followers = documentSnapshot.getLong("followerCount");
                        Long following = documentSnapshot.getLong("followingCount");

                        if (tvName != null) tvName.setText(name);
                        if (tvFollowerCount != null) tvFollowerCount.setText(String.valueOf(followers != null ? followers : 0));
                        if (tvFollowingCount != null) tvFollowingCount.setText(String.valueOf(following != null ? following : 0));
                    }
                });
    }

    private void fetchOtherUserPosts() {
        db.collection("posts")
                .whereEqualTo("author", otherUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        userPostsList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                userPostsList.add(post);
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileListener != null) profileListener.remove();
    }
}