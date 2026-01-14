package com.example.knowly;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private DatabaseReference postRef;
    private EditText commentInput;
    private TextView postContent, postAuthor, upvoteNum, downvoteNum, commentNum, categoryText;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postdetails_activity);

        // 1. Get Post ID from Intent
        postId = getIntent().getStringExtra("POST_ID");

        if (postId == null) {
            Toast.makeText(this, "Error: Post not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Initialize Views (Matching your XML IDs)
        initViews();

        // 3. Setup Firebase Reference
        postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);

        // 4. Load Data
        loadPostDetails();

        // 5. Click Listeners
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.btnSubmitComment).setOnClickListener(v -> submitComment());

        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);

// Call this to start listening for comments
        loadComments();
    }

    private void initViews() {
        // These are inside your <include android:id="@+id/includedPostCard" ... />
        postContent = findViewById(R.id.post_content);
        postAuthor = findViewById(R.id.username);
        upvoteNum = findViewById(R.id.upvote_num);
        downvoteNum = findViewById(R.id.downvote_num);
        commentNum = findViewById(R.id.comment_num);
        categoryText = findViewById(R.id.category_text);

        // Comment section views
        commentInput = findViewById(R.id.CommentInput);
    }

    private void loadPostDetails() {
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                if (post != null) {
                    // Update the Post Card UI
                    postContent.setText(post.getContent());
                    postAuthor.setText(post.getAuthor());
                    upvoteNum.setText(String.valueOf(post.getUpvote_num()));
                    downvoteNum.setText(String.valueOf(post.getDownvote_num()));
                    commentNum.setText(String.valueOf(post.getComment_num()));

                    // Handle Category visibility
                    if (post.getCategories() != null && !post.getCategories().isEmpty()) {
                        categoryText.setText(post.getCategories().get(0));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading post: " + error.getMessage());
            }
        });
    }

    private void submitComment() {
        String commentText = commentInput.getText().toString().trim();

        if (commentText.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(this, "You must be logged in to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate unique ID for the comment
        DatabaseReference commentsRef = postRef.child("comments");
        String commentId = commentsRef.push().getKey();

        // Build Comment Data
        HashMap<String, Object> commentMap = new HashMap<>();
        commentMap.put("text", commentText);
        commentMap.put("authorId", userId);
        commentMap.put("timestamp", ServerValue.TIMESTAMP);

        if (commentId != null) {
            commentsRef.child(commentId).setValue(commentMap)
                    .addOnSuccessListener(aVoid -> {
                        commentInput.setText(""); // Clear the box
                        updateCommentCount();
                        Toast.makeText(PostDetailsActivity.this, "Comment added!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PostDetailsActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateCommentCount() {
        // Increment the comment_num field in Firebase
        postRef.child("comment_num").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long currentCount = 0;
                if (task.getResult().exists()) {
                    currentCount = (long) task.getResult().getValue();
                }
                postRef.child("comment_num").setValue(currentCount + 1);
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
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}