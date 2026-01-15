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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;
    private String postId;
    private FirebaseFirestore db;

    public CommentAdapter(List<Comment> commentList, String postId) {
        this.commentList = commentList;
        this.postId = postId;
        this.db = FirebaseFirestore.getInstance();
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

        // 1. Fetch real username from Firestore "users" collection
        String uid = comment.getAuthorId();
        if (uid != null) {
            holder.userName.setTag(uid); // Tag to prevent recycling mismatch
            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && uid.equals(holder.userName.getTag())) {
                    String name = documentSnapshot.getString("username");
                    holder.userName.setText(name != null ? name : "User");
                    if (name != null && !name.isEmpty()) {
                        holder.profileInitial.setText(name.substring(0, 1).toUpperCase());
                    }
                }
            });
        }

        // 2. Set Time Ago (Handling Firestore long timestamp)
        if (comment.getTimestamp() != 0) {
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                    comment.getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS);
            holder.commentTime.setText(timeAgo);
        } else {
            holder.commentTime.setText("Just now");
        }

        // 3. Long Press to Delete (Only if you are the author)
        holder.itemView.setOnLongClickListener(v -> {
            String currentUid = FirebaseAuth.getInstance().getUid();
            if (currentUid != null && currentUid.equals(comment.getAuthorId())) {
                showDeleteDialog(v, comment);
            }
            return true;
        });
    }

    private void showDeleteDialog(View v, Comment comment) {
        new AlertDialog.Builder(v.getContext())
                .setTitle("Delete Comment")
                .setMessage("Do you want to delete this comment?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Find the specific comment document in Firestore
                    db.collection("posts").document(postId)
                            .collection("comments")
                            .whereEqualTo("timestamp", comment.getTimestamp())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    // 1. Delete the document
                                    doc.getReference().delete().addOnSuccessListener(aVoid -> {
                                        // 2. Decrement the comment count
                                        db.collection("posts").document(postId)
                                                .update("comment_num", FieldValue.increment(-1));

                                        Toast.makeText(v.getContext(), "Comment deleted", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
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