package com.example.knowly;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OthersProfileActivity extends AppCompatActivity {

    // UI Elements
    private TextView tvUsername, tvFollowerCount, btnFollowText;
    private CardView btnFollow;
    private ImageButton backButton;
    private ImageView profileImageView;
    private RecyclerView rvOtherUserPosts;

    // Stats Elements
    private TextView tvPostsCount, tvUpvotesCount, tvCommentsCount;

    // Firebase & Data
    private String targetUserId, currentUserId;
    private PostAdapter postAdapter;
    private List<Post> otherUserPosts;
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view_other);

        // 1. Get Data from Intent
        targetUserId = getIntent().getStringExtra("ownerId");
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (targetUserId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Initialize Views
        tvUsername = findViewById(R.id.other_username_text);
        tvFollowerCount = findViewById(R.id.tvFollowers);
        btnFollow = findViewById(R.id.btnFollow);
        btnFollowText = findViewById(R.id.btnFollowTextView);
        backButton = findViewById(R.id.backButton_APV);
        profileImageView = findViewById(R.id.pfp_APV); // Fixed PFP ID
        rvOtherUserPosts = findViewById(R.id.rvOtherUserPosts);

        tvPostsCount = findViewById(R.id.tv_posts_count);
        tvUpvotesCount = findViewById(R.id.tv_upvotes_count);
        tvCommentsCount = findViewById(R.id.tv_comments_count);

        // 3. Setup RecyclerView
        rvOtherUserPosts.setLayoutManager(new LinearLayoutManager(this));
        otherUserPosts = new ArrayList<>();
        postAdapter = new PostAdapter(otherUserPosts);
        rvOtherUserPosts.setAdapter(postAdapter);

        // 4. Load Data & Listeners
        loadTargetUserInfo();
        loadTargetUserPosts();
        checkFollowStatus();
        loadUserStats();

        // 5. Click Listeners
        btnFollow.setOnClickListener(v -> toggleFollow());
        backButton.setOnClickListener(v -> finish());
    }

    private void loadTargetUserInfo() {
        FirebaseDatabase.getInstance().getReference("Users").child(targetUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Load Username
                            String username = snapshot.child("username").getValue(String.class);
                            tvUsername.setText(username);
                            TextView headerTitle = findViewById(R.id.username_text_APV);
                            if (headerTitle != null) headerTitle.setText(username);

                            // LOAD PROFILE PICTURE
                            String pfpUrl = snapshot.child("profileImageUrl").getValue(String.class);
                            if (pfpUrl != null && !pfpUrl.isEmpty()) {
                                Glide.with(OthersProfileActivity.this)
                                        .load(pfpUrl)
                                        .placeholder(R.drawable.back_arrow) // Use a better placeholder if you have one
                                        .circleCrop()
                                        .into(profileImageView);
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadUserStats() {
        // --- LIVE FOLLOWER / FOLLOWING COUNT ---
        FirebaseDatabase.getInstance().getReference("Users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int followers = 0;
                        for (DataSnapshot user : snapshot.getChildren()) {
                            DataSnapshot followingList = user.child("following");
                            if (followingList.exists()) {
                                for (DataSnapshot followEntry : followingList.getChildren()) {
                                    // Make sure this matches the targetUserId we are viewing
                                    if (targetUserId.equals(followEntry.getValue(String.class))) {
                                        followers++;
                                    }
                                }
                            }
                        }

                        long following = snapshot.child(targetUserId).child("following").getChildrenCount();
                        tvFollowerCount.setText(followers + " Followers   " + following + " Following");
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        // --- LIVE UPVOTES / COMMENTS COUNT ---
        FirebaseDatabase.getInstance().getReference("Posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int totalUpvotes = 0;
                        int totalComments = 0;
                        for (DataSnapshot postSnap : snapshot.getChildren()) {
                            Post post = postSnap.getValue(Post.class);
                            if (post != null && targetUserId.equals(post.getAuthor())) {
                                if (postSnap.hasChild("upvotes")) {
                                    totalUpvotes += postSnap.child("upvotes").getChildrenCount();
                                }
                                if (postSnap.hasChild("comments")) {
                                    totalComments += postSnap.child("comments").getChildrenCount();
                                }
                            }
                        }
                        tvUpvotesCount.setText(String.valueOf(totalUpvotes));
                        tvCommentsCount.setText(String.valueOf(totalComments));
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadTargetUserPosts() {
        FirebaseDatabase.getInstance().getReference("Posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        otherUserPosts.clear();
                        int postCount = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Post post = ds.getValue(Post.class);
                            if (post != null && targetUserId.equals(post.getAuthor())) {
                                post.setPostId(ds.getKey());
                                otherUserPosts.add(0, post);
                                postCount++;
                            }
                        }
                        tvPostsCount.setText(String.valueOf(postCount));
                        postAdapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void checkFollowStatus() {
        if (currentUserId == null) return;
        FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId).child("following")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isFollowing = false;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (targetUserId.equals(ds.getValue(String.class))) {
                                isFollowing = true;
                                break;
                            }
                        }
                        updateFollowButtonUI();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void updateFollowButtonUI() {
        if (btnFollowText == null) return;
        if (isFollowing) {
            btnFollowText.setText("Unfollow");
            btnFollowText.setBackground(null);
            btnFollow.setCardBackgroundColor(Color.parseColor("#BDBDBD"));
        } else {
            btnFollowText.setText("Follow");
            btnFollowText.setBackgroundResource(R.drawable.button_gradient);
            btnFollow.setCardBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void toggleFollow() {
        if (currentUserId == null) return;
        if (currentUserId.equals(targetUserId)) {
            Toast.makeText(this, "You cannot follow yourself", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference myFollowingRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId).child("following");

        if (isFollowing) {
            myFollowingRef.orderByValue().equalTo(targetUserId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ds.getRef().removeValue();
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
        } else {
            myFollowingRef.push().setValue(targetUserId);
        }
    }
}