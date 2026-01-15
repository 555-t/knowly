package com.example.knowly;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
        return (FirebaseAuth.getInstance().getCurrentUser() != null)
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
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

        // Safety check to prevent crash if data is missing
        if (post == null || userId == null) return;

        // 1. Set Content
        holder.content.setText(post.getContent());

        // 2. Profile & Navigation Logic (The Fix)
        String authorUid = post.getAuthor();
        holder.author.setTag(authorUid);

        // Reset UI for recycled views to avoid showing wrong user info
        holder.author.setText("...");
        holder.postInitial.setVisibility(View.VISIBLE);
        holder.profilePic.setImageResource(R.drawable.chip_cat_gradient_checked);

        if (authorUid != null && !authorUid.isEmpty()) {
            View.OnClickListener toProfile = v -> {
                Intent intent;
                // Check if the author is the current logged-in user
                if (authorUid.equals(userId)) {
                    intent = new Intent(v.getContext(), UserPageActivity.class);
                } else {
                    // It's someone else, go to OthersProfileActivity
                    intent = new Intent(v.getContext(), OthersProfileActivity.class);
                    // MANDATORY: Use "USER_ID" to match OthersProfileActivity's getIntent()
                    intent.putExtra("USER_ID", authorUid);
                }
                v.getContext().startActivity(intent);
            };

            // Set clicks for all profile parts
            holder.author.setOnClickListener(toProfile);
            holder.profilePic.setOnClickListener(toProfile);
            holder.postInitial.setOnClickListener(toProfile);

            // Fetch Username/PFP from Realtime Database
            FirebaseDatabase.getInstance().getReference("Users").child(authorUid)
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            // Only update if the holder is still showing the same author (safe scroll)
                            if (authorUid.equals(holder.author.getTag())) {
                                String name = task.getResult().child("username").getValue(String.class);
                                String pfpUrl = task.getResult().child("profileImageUrl").getValue(String.class);

                                holder.author.setText(name != null ? name : "User");

                                if (pfpUrl != null && !pfpUrl.isEmpty()) {
                                    holder.postInitial.setVisibility(View.GONE);
                                    Glide.with(holder.itemView.getContext())
                                            .load(pfpUrl)
                                            .circleCrop()
                                            .into(holder.profilePic);
                                } else if (name != null && !name.isEmpty()) {
                                    holder.postInitial.setText(name.substring(0, 1).toUpperCase());
                                }
                            }
                        }
                    });
        }

        // 3. Stats & Metadata
        holder.timeOfPost.setText(getTimeAgo(post.getTimestamp()));
        holder.upvoteNum.setText(String.valueOf(post.getUpvote_num()));
        holder.downvoteNum.setText(String.valueOf(post.getDownvote_num()));
        holder.commentNum.setText(String.valueOf(post.getComment_num()));

        // 4. Categories
        holder.categoryGroup.removeAllViews();
        if (post.getCategories() != null) {
            for (String cat : post.getCategories()) {
                TextView tv = new TextView(holder.itemView.getContext());
                tv.setText(cat);
                tv.setBackgroundResource(R.drawable.bg_category_gradient);
                tv.setTextColor(Color.parseColor("#2788A0"));
                tv.setPadding(20, 10, 20, 10);
                holder.categoryGroup.addView(tv);
            }
        }

        // 5. Interaction Listeners
        updateVoteUI(holder, post, userId);
        if (post.getPostId() != null) {
            DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(post.getPostId());
            holder.upvoteImg.setOnClickListener(v -> toggleVote(postRef, "upvotes", "downvotes", userId, post));
            holder.downvoteImg.setOnClickListener(v -> toggleVote(postRef, "downvotes", "upvotes", userId, post));
        }

        holder.commentImg.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), PostDetailsActivity.class);
            i.putExtra("POST_ID", post.getPostId());
            v.getContext().startActivity(i);
        });

        holder.moreBtn.setOnClickListener(v -> showPopup(v, post, userId));
    }

    // --- Helper Methods ---

    private void toggleVote(DatabaseReference ref, String node, String otherNode, String uid, Post post) {
        if (node.equals("upvotes") && post.getUpvotes() != null && post.getUpvotes().containsKey(uid)) {
            ref.child(node).child(uid).removeValue();
        } else if (node.equals("downvotes") && post.getDownvotes() != null && post.getDownvotes().containsKey(uid)) {
            ref.child(node).child(uid).removeValue();
        } else {
            ref.child(node).child(uid).setValue(true);
            ref.child(otherNode).child(uid).removeValue();
        }
    }

    private String getTimeAgo(long time) {
        long diff = System.currentTimeMillis() - time;
        if (diff < 60000) return "just now";
        if (diff < 3600000) return (diff / 60000) + "m ago";
        if (diff < 86400000) return (diff / 3600000) + "h ago";
        return (diff / 86400000) + "d ago";
    }

    private void updateVoteUI(ViewHolder h, Post p, String id) {
        int active = Color.parseColor("#3498db");
        int gray = Color.parseColor("#808080");
        h.upvoteImg.setColorFilter(p.getUpvotes() != null && p.getUpvotes().containsKey(id) ? active : gray);
        h.downvoteImg.setColorFilter(p.getDownvotes() != null && p.getDownvotes().containsKey(id) ? Color.RED : gray);
    }

    private void showPopup(View v, Post p, String id) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        if (p.getAuthor() != null && p.getAuthor().equals(id)) popup.getMenu().add("Delete");
        else popup.getMenu().add("Report");
        popup.show();
    }

    @Override
    public int getItemCount() { return postList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView content, author, postInitial, timeOfPost, upvoteNum, downvoteNum, commentNum;
        ImageView upvoteImg, downvoteImg, commentImg, bookmarkBtn, profilePic;
        ChipGroup categoryGroup;
        ImageButton moreBtn;

        public ViewHolder(View v) {
            super(v);
            content = v.findViewById(R.id.post_content);
            author = v.findViewById(R.id.username);
            timeOfPost = v.findViewById(R.id.time_of_post);
            categoryGroup = v.findViewById(R.id.category_chip_group);
            postInitial = v.findViewById(R.id.post_initial);
            profilePic = v.findViewById(R.id.profile_pic);
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
    public void updateList(List<Post> newList) {
        this.postList = newList;
        notifyDataSetChanged();
    }
}