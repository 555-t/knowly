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

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    private String getCurrentUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
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

        // --- 1. Set post content ---
        holder.content.setText(post.getContent());

        // --- 2. FETCH REAL USERNAME ---
        String authorUid = post.getAuthor();
        holder.author.setTag(authorUid);
        holder.author.setText("...");
        holder.postInitial.setText("?");

        if (authorUid != null) {
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(authorUid)
                    .child("username")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (holder.author.getTag() != null && holder.author.getTag().equals(authorUid)) {
                            if (task.isSuccessful() && task.getResult().exists()) {
                                String name = String.valueOf(task.getResult().getValue());
                                holder.author.setText(name);
                                if (name != null && !name.isEmpty()) {
                                    holder.postInitial.setText(name.substring(0, 1).toUpperCase());
                                }
                            } else {
                                holder.author.setText("User " + authorUid.substring(0, 4));
                                holder.postInitial.setText("U");
                            }
                        }
                    });
        }

        // --- 3. TIMESTAMP LOGIC ---
        if (post.getTimestamp() != 0) {
            holder.timeOfPost.setText(getTimeAgo(post.getTimestamp()));
        } else {
            holder.timeOfPost.setText("just now");
        }

        // --- 4. Set the interaction numbers ---
        holder.upvoteNum.setText(String.valueOf(post.getUpvote_num()));
        holder.downvoteNum.setText(String.valueOf(post.getDownvote_num()));
        holder.commentNum.setText(String.valueOf(post.getComment_num()));

        // --- 5. MULTIPLE CATEGORY LOGIC (RESTORED ORIGINAL DESIGN) ---
        holder.categoryGroup.removeAllViews();
        if (post.getCategories() != null && !post.getCategories().isEmpty()) {
            holder.categoryGroup.setVisibility(View.VISIBLE);
            for (String catName : post.getCategories()) {
                // Create a TextView for the exact bubble look
                TextView tv = new TextView(holder.itemView.getContext());
                tv.setText(catName);

                // Apply your exact gradient background
                tv.setBackgroundResource(R.drawable.bg_category_gradient);

                // Exact styling
                tv.setTextColor(Color.parseColor("#2788A0"));
                tv.setTextSize(12); // 12sp
                tv.setAllCaps(false);
                tv.setGravity(android.view.Gravity.CENTER);

                // Convert 10dp and 4dp to pixels for padding
                float scale = holder.itemView.getContext().getResources().getDisplayMetrics().density;
                int padSide = (int) (10 * scale + 0.5f);
                int padTopBottom = (int) (4 * scale + 0.5f);
                tv.setPadding(padSide, padTopBottom, padSide, padTopBottom);

                // Layout margins for spacing between the bubbles
                com.google.android.material.chip.ChipGroup.LayoutParams params =
                        new com.google.android.material.chip.ChipGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 6, 6);
                tv.setLayoutParams(params);

                holder.categoryGroup.addView(tv);
            }
        } else {
            holder.categoryGroup.setVisibility(View.GONE);
        }

        // --- 6. UI & Interaction Logic ---
        updateVoteUI(holder, post, userId);

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
                    NotificationUtils.sendNotification(post.getAuthor(), "comment", "upvoted your post");
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

            // --- BOOKMARK LOGIC ---
            holder.bookmarkBtn.setOnClickListener(v -> {
                boolean isCurrentlySelected = holder.bookmarkBtn.isSelected();
                holder.bookmarkBtn.setSelected(!isCurrentlySelected);

                if (holder.bookmarkBtn.isSelected()) {
                    Toast.makeText(v.getContext(), "Post Bookmarked", Toast.LENGTH_SHORT).show();
                }
            });
        }

        holder.commentImg.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PostDetailsActivity.class);
            intent.putExtra("POST_ID", post.getPostId());
            v.getContext().startActivity(intent);
        });

        holder.moreBtn.setOnClickListener(v -> showPopupMenu(v, post, userId));
    }

    private String getTimeAgo(long time) {
        long now = System.currentTimeMillis();
        if (time > now || time <= 0) return "just now";

        final long diff = now - time;
        if (diff < 60000) return "just now";
        if (diff < 3600000) return (diff / 60000) + "m ago";
        if (diff < 86400000) return (diff / 3600000) + "h ago";
        if (diff < 604800000) return (diff / 86400000) + "d ago";
        return (diff / 604800000) + "w ago";
    }

    private void updateVoteUI(ViewHolder holder, Post post, String userId) {
        int activeColor = Color.parseColor("#3498db");
        int downColor = Color.parseColor("#e74c3c");
        int grayColor = Color.parseColor("#808080");

        if (post.getUpvotes() != null && post.getUpvotes().containsKey(userId)) {
            holder.upvoteImg.setColorFilter(activeColor);
        } else {
            holder.upvoteImg.setColorFilter(grayColor);
        }

        if (post.getDownvotes() != null && post.getDownvotes().containsKey(userId)) {
            holder.downvoteImg.setColorFilter(downColor);
        } else {
            holder.downvoteImg.setColorFilter(grayColor);
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
        TextView content, author, postInitial, timeOfPost;
        ChipGroup categoryGroup; // Updated from TextView to ChipGroup
        TextView upvoteNum, downvoteNum, commentNum;
        ImageView upvoteImg, downvoteImg, commentImg, bookmarkBtn;
        ImageButton moreBtn;

        public ViewHolder(View v) {
            super(v);
            content = v.findViewById(R.id.post_content);
            author = v.findViewById(R.id.username);
            timeOfPost = v.findViewById(R.id.time_of_post);
            categoryGroup = v.findViewById(R.id.category_chip_group); // Map the ChipGroup
            postInitial = v.findViewById(R.id.post_initial);
            upvoteNum = v.findViewById(R.id.upvote_num);
            downvoteNum = v.findViewById(R.id.downvote_num);
            commentNum = v.findViewById(R.id.comment_num);
            upvoteImg = v.findViewById(R.id.upvote_img);
            downvoteImg = v.findViewById(R.id.downvote_img);
            commentImg = v.findViewById(R.id.comment_img);
            bookmarkBtn = v.findViewById(R.id.bookmark_btn);
            moreBtn = v.findViewById(R.id.imageButton);
        }
    }
}