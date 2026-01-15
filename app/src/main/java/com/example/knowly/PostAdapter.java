package com.example.knowly;

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

import com.bumptech.glide.Glide;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> postList;
    private Context context;
    private FirebaseFirestore db;

    public PostAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
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

        if (post == null || userId == null) return;

        // Content & Metadata
        holder.content.setText(post.getContent());
        holder.timeOfPost.setText(getTimeAgo(post.getTimestamp()));
        holder.upvoteNum.setText(String.valueOf(post.getUpvote_num()));
        holder.downvoteNum.setText(String.valueOf(post.getDownvote_num()));
        holder.commentNum.setText(String.valueOf(post.getComment_num()));

        // --- Profile Logic (Firestore) ---
        String authorUid = post.getAuthor();
        holder.author.setTag(authorUid);
        holder.author.setText("...");
        holder.postInitial.setVisibility(View.VISIBLE);

        if (authorUid != null && !authorUid.isEmpty()) {
            View.OnClickListener toProfile = v -> {
                Intent intent = authorUid.equals(userId) ?
                        new Intent(context, UserPageActivity.class) :
                        new Intent(context, OthersProfileActivity.class).putExtra("USER_ID", authorUid);
                context.startActivity(intent);
            };

            holder.author.setOnClickListener(toProfile);
            holder.profilePic.setOnClickListener(toProfile);
            holder.postInitial.setOnClickListener(toProfile);

            // Fetching from Firestore "users" collection
            db.collection("users").document(authorUid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && authorUid.equals(holder.author.getTag())) {
                    String name = documentSnapshot.getString("username");
                    String pfpUrl = documentSnapshot.getString("profileImageUrl");
                    holder.author.setText(name != null ? name : "User");

                    if (pfpUrl != null && !pfpUrl.isEmpty()) {
                        holder.postInitial.setVisibility(View.GONE);
                        Glide.with(context).load(pfpUrl).circleCrop().into(holder.profilePic);
                    } else if (name != null && !name.isEmpty()) {
                        holder.postInitial.setText(name.substring(0, 1).toUpperCase());
                    }
                }
            });
        }

        // --- Categories ---
        holder.categoryGroup.removeAllViews();
        if (post.getCategories() != null) {
            for (String cat : post.getCategories()) {
                TextView tv = new TextView(context);
                tv.setText(cat);
                tv.setBackgroundResource(R.drawable.bg_category_gradient);
                tv.setTextColor(Color.parseColor("#2788A0"));
                tv.setPadding(20, 10, 20, 10);
                holder.categoryGroup.addView(tv);
            }
        }

        // --- Interactions (Firestore Vote & Bookmark) ---
        DocumentReference postRef = db.collection("posts").document(post.getPostId());

        // 1. Initial Vote State Listener
        postRef.collection("votes").document(userId).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                String type = snapshot.getString("type");
                holder.upvoteImg.setSelected("up".equals(type));
                holder.downvoteImg.setSelected("down".equals(type));
            } else {
                holder.upvoteImg.setSelected(false);
                holder.downvoteImg.setSelected(false);
            }
        });

        // 2. Vote Click Actions
        holder.upvoteImg.setOnClickListener(v -> handleVote(post.getPostId(), "up", holder.upvoteImg.isSelected()));
        holder.downvoteImg.setOnClickListener(v -> handleVote(post.getPostId(), "down", holder.downvoteImg.isSelected()));

        // --- Bookmark Logic ---
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                List<String> bookmarks = (List<String>) snapshot.get("bookmarks");
                holder.bookmarkBtn.setSelected(bookmarks != null && bookmarks.contains(post.getPostId()));
            }
        });

        holder.bookmarkBtn.setOnClickListener(v -> {
            if (holder.bookmarkBtn.isSelected()) {
                userRef.update("bookmarks", FieldValue.arrayRemove(post.getPostId()));
            } else {
                userRef.update("bookmarks", FieldValue.arrayUnion(post.getPostId()));
            }
        });

        holder.commentImg.setOnClickListener(v -> {
            context.startActivity(new Intent(context, PostDetailsActivity.class).putExtra("POST_ID", post.getPostId()));
        });

        holder.moreBtn.setOnClickListener(v -> showPopup(v, post, userId));
    }

    private void handleVote(String postId, String type, boolean isSelected) {
        String userId = getCurrentUserId();
        DocumentReference postRef = db.collection("posts").document(postId);
        DocumentReference voteRef = postRef.collection("votes").document(userId);

        if (isSelected) {
            // User clicked the same button again -> Remove vote
            voteRef.delete().addOnSuccessListener(aVoid -> {
                String field = type.equals("up") ? "upvote_num" : "downvote_num";
                postRef.update(field, FieldValue.increment(-1));
            });
        } else {
            // Check if they are switching from the opposite vote
            voteRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String oldType = snapshot.getString("type");
                    if (oldType != null && !oldType.equals(type)) {
                        // Switching: Decrement the old one
                        String oldField = oldType.equals("up") ? "upvote_num" : "downvote_num";
                        postRef.update(oldField, FieldValue.increment(-1));
                    }
                }

                // Apply new vote
                Map<String, Object> voteData = new HashMap<>();
                voteData.put("type", type);
                voteRef.set(voteData).addOnSuccessListener(aVoid -> {
                    String newField = type.equals("up") ? "upvote_num" : "downvote_num";
                    postRef.update(newField, FieldValue.increment(1));

                    // ADDED: Send notification for Upvotes only
                    if (type.equals("up")) {
                        db.collection("posts").document(postId).get().addOnSuccessListener(postDoc -> {
                            String ownerId = postDoc.getString("author");
                            NotificationUtils.sendNotification(ownerId, "upvote", "upvoted your post!", postId);
                        });
                    }
                });
            });
        }
    }
    private String getTimeAgo(long time) {
        long diff = System.currentTimeMillis() - time;
        if (diff < 60000) return "just now";
        if (diff < 3600000) return (diff / 60000) + "m ago";
        if (diff < 86400000) return (diff / 3600000) + "h ago";
        return (diff / 86400000) + "d ago";
    }

    private void showPopup(View v, Post p, String id) {
        PopupMenu popup = new PopupMenu(context, v);
        if (p.getAuthor() != null && p.getAuthor().equals(id)) {
            popup.getMenu().add("Delete").setOnMenuItemClickListener(item -> {
                db.collection("posts").document(p.getPostId()).delete();
                return true;
            });
        } else {
            popup.getMenu().add("Report");
        }
        popup.show();
    }
    // Add this method inside your PostAdapter class (e.g., above getItemCount)
    public void updateList(List<Post> newList) {
        this.postList = newList;
        notifyDataSetChanged();
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
}