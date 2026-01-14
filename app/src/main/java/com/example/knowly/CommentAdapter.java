package com.example.knowly;

import android.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;
    private String postId; // Added to handle deletions

    public CommentAdapter(List<Comment> commentList, String postId) {
        this.commentList = commentList;
        this.postId = postId;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        holder.commentText.setText(comment.getText());

        // 1. Fetch real username from Firebase "Users" node
        String uid = comment.getAuthorId();
        if (uid != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            userRef.child("username").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    String name = task.getResult().getValue().toString();
                    holder.userName.setText(name);
                    holder.profileInitial.setText(name.substring(0, 1).toUpperCase());
                } else {
                    holder.userName.setText("User");
                    holder.profileInitial.setText("U");
                }
            });
        }

        // 2. Set Time Ago
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                comment.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS);
        holder.commentTime.setText(timeAgo);

        // 3. Long Press to Delete (Only if you are the author)
        holder.itemView.setOnLongClickListener(v -> {
            String currentUid = FirebaseAuth.getInstance().getUid();
            if (currentUid != null && currentUid.equals(comment.getAuthorId())) {
                showDeleteDialog(v, comment, position);
            }
            return true;
        });
    }

    private void showDeleteDialog(View v, Comment comment, int position) {
        new AlertDialog.Builder(v.getContext())
                .setTitle("Delete Comment")
                .setMessage("Do you want to delete this comment?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    DatabaseReference postRef = FirebaseDatabase.getInstance()
                            .getReference("Posts")
                            .child(postId);

                    DatabaseReference commentsRef = postRef.child("comments");

                    commentsRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (com.google.firebase.database.DataSnapshot ds : task.getResult().getChildren()) {
                                Comment c = ds.getValue(Comment.class);
                                if (c != null && c.getTimestamp() == comment.getTimestamp()) {
                                    // 1. Remove the comment
                                    ds.getRef().removeValue().addOnSuccessListener(aVoid -> {
                                        // 2. Decrement the comment count on the post
                                        decrementCommentCount(postRef);
                                        Toast.makeText(v.getContext(), "Comment deleted", Toast.LENGTH_SHORT).show();
                                    });
                                    break;
                                }
                            }
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void decrementCommentCount(DatabaseReference postRef) {
        postRef.child("comment_num").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                long currentCount = (long) task.getResult().getValue();
                if (currentCount > 0) {
                    postRef.child("comment_num").setValue(currentCount - 1);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userName, commentText, commentTime, profileInitial;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.tvCommentName);
            commentText = itemView.findViewById(R.id.tvCommentText);
            commentTime = itemView.findViewById(R.id.tvCommentTime);
            profileInitial = itemView.findViewById(R.id.comment_initial);
        }
    }
}