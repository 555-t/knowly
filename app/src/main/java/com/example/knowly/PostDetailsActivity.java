package com.example.knowly;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostDetailsActivity extends AppCompatActivity {

    private String postId;
    private String postOwnerId;
    private DatabaseReference postRef;
    private EditText commentInput;
    private TextView postContent, postAuthor, upvoteNum, downvoteNum, commentNum, postInitial;
    private ChipGroup categoryGroup;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postdetails_activity);

        postId = getIntent().getStringExtra("POST_ID");

        if (postId == null) {
            Toast.makeText(this, "Error: Post not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);

        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList, postId);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);

        loadPostDetails();
        loadComments();

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.btnSubmitComment).setOnClickListener(v -> submitComment());
    }

    private void initViews() {
        View postCard = findViewById(R.id.includedPostCard);
        postContent = postCard.findViewById(R.id.post_content);
        postAuthor = postCard.findViewById(R.id.username);
        postInitial = postCard.findViewById(R.id.post_initial);
        upvoteNum = postCard.findViewById(R.id.upvote_num);
        downvoteNum = postCard.findViewById(R.id.downvote_num);
        commentNum = postCard.findViewById(R.id.comment_num);
        categoryGroup = postCard.findViewById(R.id.category_chip_group);
        commentInput = findViewById(R.id.CommentInput);
    }

    private void loadPostDetails() {
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                if (post != null) {
                    postOwnerId = post.getAuthor();
                    postContent.setText(post.getContent());

                    if (postOwnerId != null) {
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(postOwnerId)
                                .child("username")
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult().exists()) {
                                        String name = String.valueOf(task.getResult().getValue());
                                        postAuthor.setText(name);
                                        if (name != null && !name.isEmpty()) {
                                            postInitial.setText(name.substring(0, 1).toUpperCase());
                                        }
                                    }
                                });
                    }

                    upvoteNum.setText(String.valueOf(post.getUpvote_num()));
                    downvoteNum.setText(String.valueOf(post.getDownvote_num()));
                    commentNum.setText(String.valueOf(post.getComment_num()));

                    // --- 5. MULTIPLE CATEGORY LOGIC (RESTORED ORIGINAL STYLE) ---
                    categoryGroup.removeAllViews(); // Prevent duplication when data updates
                    if (post.getCategories() != null && !post.getCategories().isEmpty()) {
                        categoryGroup.setVisibility(View.VISIBLE);
                        for (String catName : post.getCategories()) {
                            // Using TextView instead of Chip to preserve gradient look perfectly
                            TextView tv = new TextView(PostDetailsActivity.this);
                            tv.setText(catName);

                            // Style settings
                            tv.setBackgroundResource(R.drawable.bg_category_gradient);
                            tv.setTextColor(Color.parseColor("#2788A0"));
                            tv.setTextSize(12);

                            // Padding: (left, top, right, bottom)
                            tv.setPadding(30, 10, 30, 10);

                            // Set margins between bubbles
                            ChipGroup.LayoutParams params = new ChipGroup.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(0, 0, 6, 6);
                            tv.setLayoutParams(params);

                            categoryGroup.addView(tv);
                        }
                    } else {
                        categoryGroup.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
            }
        });
    }

    private void submitComment() {
        String commentText = commentInput.getText().toString().trim();
        if (commentText.isEmpty()) return;

        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        DatabaseReference commentsRef = postRef.child("comments");
        String commentId = commentsRef.push().getKey();

        HashMap<String, Object> commentMap = new HashMap<>();
        commentMap.put("text", commentText);
        commentMap.put("authorId", currentUserId);
        commentMap.put("timestamp", ServerValue.TIMESTAMP);

        if (commentId != null) {
            commentsRef.child(commentId).setValue(commentMap).addOnSuccessListener(aVoid -> {
                if (postOwnerId != null && !postOwnerId.equals(currentUserId)) {
                    NotificationUtils.sendNotification(postOwnerId, "comment", "commented on your post");
                }
                commentInput.setText("");
                updateCommentCount(true);
            });
        }
    }

    private void updateCommentCount(boolean increment) {
        postRef.child("comment_num").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long currentCount = 0;
                if (task.getResult().exists()) {
                    Object val = task.getResult().getValue();
                    if (val instanceof Long) currentCount = (Long) val;
                }
                long newCount = increment ? currentCount + 1 : Math.max(0, currentCount - 1);
                postRef.child("comment_num").setValue(newCount);
            }
        });
    }

    private void loadComments() {
        postRef.child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Comment comment = ds.getValue(Comment.class);
                    if (comment != null) commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}