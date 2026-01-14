package com.example.knowly;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private TextView postContent, postAuthor, upvoteNum, downvoteNum, commentNum, categoryText, postInitial;
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
        // Find the included card first
        View postCard = findViewById(R.id.includedPostCard);

        // Find views INSIDE that card
        postContent = postCard.findViewById(R.id.post_content);
        postAuthor = postCard.findViewById(R.id.username);
        postInitial = postCard.findViewById(R.id.post_initial); // NEW: Matches item_post.xml
        upvoteNum = postCard.findViewById(R.id.upvote_num);
        downvoteNum = postCard.findViewById(R.id.downvote_num);
        commentNum = postCard.findViewById(R.id.comment_num);
        categoryText = postCard.findViewById(R.id.category_text);

        // Find views in the activity layout
        commentInput = findViewById(R.id.CommentInput);
    }

    private void loadPostDetails() {
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                if (post != null) {
                    postContent.setText(post.getContent());

                    // Fetch Username and set PFP Initial
                    String authorUid = post.getAuthor();
                    if (authorUid != null) {
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(authorUid)
                                .child("username")
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult().exists()) {
                                        String name = String.valueOf(task.getResult().getValue());
                                        postAuthor.setText(name);

                                        // Set the "C" for chihuahua etc.
                                        if (name != null && !name.isEmpty()) {
                                            postInitial.setText(name.substring(0, 1).toUpperCase());
                                        }
                                    } else {
                                        // Fallback for student_user
                                        postAuthor.setText(authorUid);
                                        if (authorUid.length() > 0) {
                                            postInitial.setText(authorUid.substring(0, 1).toUpperCase());
                                        }
                                    }
                                });
                    }

                    upvoteNum.setText(String.valueOf(post.getUpvote_num()));
                    downvoteNum.setText(String.valueOf(post.getDownvote_num()));
                    commentNum.setText(String.valueOf(post.getComment_num()));

                    if (post.getCategories() != null && !post.getCategories().isEmpty()) {
                        categoryText.setText(post.getCategories().get(0));
                        categoryText.setVisibility(View.VISIBLE);
                    } else {
                        categoryText.setVisibility(View.GONE);
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

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        DatabaseReference commentsRef = postRef.child("comments");
        String commentId = commentsRef.push().getKey();

        HashMap<String, Object> commentMap = new HashMap<>();
        commentMap.put("text", commentText);
        commentMap.put("authorId", userId);
        commentMap.put("timestamp", ServerValue.TIMESTAMP);

        if (commentId != null) {
            commentsRef.child(commentId).setValue(commentMap).addOnSuccessListener(aVoid -> {
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
                    currentCount = (long) task.getResult().getValue();
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