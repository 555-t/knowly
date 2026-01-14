package com.example.knowly;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> postList;

    private String getCurrentUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        String userId = getCurrentUserId();
        if (userId == null) return;

        // 1. Set post content
        holder.content.setText(post.getContent());

        // 2. FETCH REAL USERNAME & SET PFP INITIAL
        String authorUid = post.getAuthor();
        if (authorUid != null) {
            holder.author.setText("Loading...");
            holder.postInitial.setText("?");

            FirebaseDatabase.getInstance().getReference("Users")
                    .child(authorUid)
                    .child("username")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            String name = String.valueOf(task.getResult().getValue());
                            holder.author.setText(name);
                            if (name != null && !name.isEmpty()) {
                                holder.postInitial.setText(name.substring(0, 1).toUpperCase());
                            }
                        } else {
                            holder.author.setText(authorUid);
                            if (authorUid.length() > 0) {
                                holder.postInitial.setText(authorUid.substring(0, 1).toUpperCase());
                            }
                        }
                    });
        }

        // 3. Set the interaction numbers
        holder.upvoteNum.setText(String.valueOf(post.getUpvote_num()));
        holder.downvoteNum.setText(String.valueOf(post.getDownvote_num()));
        holder.commentNum.setText(String.valueOf(post.getComment_num()));

        // 4. Category logic
        if (post.getCategories() != null && !post.getCategories().isEmpty()) {
            holder.category.setText(post.getCategories().get(0));
            holder.category.setVisibility(View.VISIBLE);
        } else {
            holder.category.setVisibility(View.GONE);
        }

        // 5. Visual Feedback for Votes (Updated logic)
        updateVoteUI(holder, post, userId);

        // 6. Voting Logic
        if (post.getPostId() != null) {
            DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(post.getPostId());

            holder.upvoteImg.setOnClickListener(v -> {
                DatabaseReference upRef = postRef.child("upvotes").child(userId);
                DatabaseReference downRef = postRef.child("downvotes").child(userId);
                if (post.getUpvotes() != null && post.getUpvotes().containsKey(userId)) {
                    upRef.removeValue();
                } else {
                    upRef.setValue(true);
                    downRef.removeValue();
                }
            });

            holder.downvoteImg.setOnClickListener(v -> {
                DatabaseReference upRef = postRef.child("upvotes").child(userId);
                DatabaseReference downRef = postRef.child("downvotes").child(userId);
                if (post.getDownvotes() != null && post.getDownvotes().containsKey(userId)) {
                    downRef.removeValue();
                } else {
                    downRef.setValue(true);
                    upRef.removeValue();
                }
            });
        }

        // 7. Navigation to Details
        holder.commentImg.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PostDetailsActivity.class);
            intent.putExtra("POST_ID", post.getPostId());
            v.getContext().startActivity(intent);
        });

        // 8. THREE DOT MENU (Delete/Report)
        holder.moreBtn.setOnClickListener(v -> {
            showPopupMenu(v, post, userId);
        });
    }

    private void updateVoteUI(ViewHolder holder, Post post, String userId) {
        // UPVOTE UI
        if (post.getUpvotes() != null && post.getUpvotes().containsKey(userId)) {
            holder.upvoteImg.setSelected(true); // Triggers 'vote_filled' in selector
            holder.upvoteImg.setColorFilter(Color.parseColor("#3498db")); // Bright Blue
        } else {
            holder.upvoteImg.setSelected(false); // Triggers 'vote_unfilled' in selector
            holder.upvoteImg.setColorFilter(Color.parseColor("#808080")); // Dark Gray
        }

        // DOWNVOTE UI
        if (post.getDownvotes() != null && post.getDownvotes().containsKey(userId)) {
            holder.downvoteImg.setSelected(true); // Triggers 'vote_filled' in selector
            holder.downvoteImg.setColorFilter(Color.parseColor("#e74c3c")); // Bright Red
        } else {
            holder.downvoteImg.setSelected(false); // Triggers 'vote_unfilled' in selector
            holder.downvoteImg.setColorFilter(Color.parseColor("#808080")); // Dark Gray
        }
    }

    private void showPopupMenu(View view, Post post, String userId) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);

        if (post.getAuthor() != null && post.getAuthor().equals(userId)) {
            popupMenu.getMenu().add("Delete Post");
        } else {
            popupMenu.getMenu().add("Report Post");
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Delete Post")) {
                showDeleteConfirmation(view.getContext(), post.getPostId());
            } else {
                Toast.makeText(view.getContext(), "Post Reported", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popupMenu.show();
    }

    private void showDeleteConfirmation(Context context, String postId) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference("Posts")
                            .child(postId).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(context, "Post removed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView content, author, category, postInitial;
        TextView upvoteNum, downvoteNum, commentNum;
        ImageView upvoteImg, downvoteImg, commentImg;
        ImageButton moreBtn;

        public ViewHolder(View v) {
            super(v);
            content = v.findViewById(R.id.post_content);
            author = v.findViewById(R.id.username);
            category = v.findViewById(R.id.category_text);
            postInitial = v.findViewById(R.id.post_initial);
            upvoteNum = v.findViewById(R.id.upvote_num);
            downvoteNum = v.findViewById(R.id.downvote_num);
            commentNum = v.findViewById(R.id.comment_num);
            upvoteImg = v.findViewById(R.id.upvote_img);
            downvoteImg = v.findViewById(R.id.downvote_img);
            commentImg = v.findViewById(R.id.comment_img);
            moreBtn = v.findViewById(R.id.imageButton);
        }
    }
}