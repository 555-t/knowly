package com.example.knowly;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDetailsActivity extends AppCompatActivity {

    private String postId;
    private String postOwnerId;
    private DocumentReference postRef; // Changed to DocumentReference
    private FirebaseFirestore db;

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

        db = FirebaseFirestore.getInstance();
        postRef = db.collection("posts").document(postId); // Reference to Firestore post

        initViews();

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
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
    }

    private void loadPostDetails() {
        // Use SnapshotListener for real-time updates (upvotes/comments)
        postRef.addSnapshotListener((snapshot, e) -> {
            if (e != null || snapshot == null || !snapshot.exists()) return;

            Post post = snapshot.toObject(Post.class);
            if (post != null) {
                postOwnerId = post.getAuthor();
                postContent.setText(post.getContent());
                upvoteNum.setText(String.valueOf(post.getUpvote_num()));
                downvoteNum.setText(String.valueOf(post.getDownvote_num()));
                commentNum.setText(String.valueOf(post.getComment_num()));

                // FETCH USERNAME FROM "users" (lowercase)
                if (postOwnerId != null) {
                    db.collection("users").document(postOwnerId).get()
                            .addOnSuccessListener(userDoc -> {
                                if (userDoc.exists()) {
                                    String name = userDoc.getString("username");
                                    postAuthor.setText(name != null ? name : "Unknown");
                                    if (name != null && !name.isEmpty()) {
                                        postInitial.setText(name.substring(0, 1).toUpperCase());
                                    }
                                }
                            });
                }

                // CATEGORY LOGIC
                categoryGroup.removeAllViews();
                if (post.getCategories() != null) {
                    categoryGroup.setVisibility(View.VISIBLE);
                    for (String catName : post.getCategories()) {
                        TextView tv = new TextView(this);
                        tv.setText(catName);
                        tv.setBackgroundResource(R.drawable.bg_category_gradient);
                        tv.setTextColor(Color.parseColor("#2788A0"));
                        tv.setPadding(30, 10, 30, 10);
                        categoryGroup.addView(tv);
                    }
                }
            }
        });
    }

// ... existing imports ...

    private void submitComment() {
        String commentText = commentInput.getText().toString().trim();
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (commentText.isEmpty() || currentUserId == null) return;

        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("text", commentText);
        commentMap.put("authorId", currentUserId);
        commentMap.put("timestamp", FieldValue.serverTimestamp());

        postRef.collection("comments").add(commentMap).addOnSuccessListener(doc -> {
            commentInput.setText("");
            updateCommentCount(true);

            // ADDED: Send notification to post owner
            if (postOwnerId != null) {
                NotificationUtils.sendNotification(
                        postOwnerId,
                        "comment",
                        "commented on your post: " + commentText,
                        postId
                );
            }
        });
    }
    private void updateCommentCount(boolean increment) {
        postRef.update("comment_num", FieldValue.increment(increment ? 1 : -1));
    }

    private void loadComments() {
        postRef.collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) return;
                    commentList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot ds : snapshot.getDocuments()) {
                        Comment comment = ds.toObject(Comment.class);
                        if (comment != null) commentList.add(comment);
                    }
                    commentAdapter.notifyDataSetChanged();
                });
    }
}