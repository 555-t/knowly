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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> postList;
    private Context context;

    public PostAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
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

        // --- Profile Logic ---
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

            FirebaseDatabase.getInstance().getReference("Users").child(authorUid)
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists() && authorUid.equals(holder.author.getTag())) {
                            String name = task.getResult().child("username").getValue(String.class);
                            String pfpUrl = task.getResult().child("profileImageUrl").getValue(String.class);
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

        // --- Interactions (RESTORED VOTE LOGIC) ---
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts").child(post.getPostId());

        // 1. Update Vote UI Initial State
        boolean isUpvoted = post.getUpvotes() != null && post.getUpvotes().containsKey(userId);
        boolean isDownvoted = post.getDownvotes() != null && post.getDownvotes().containsKey(userId);
        holder.upvoteImg.setSelected(isUpvoted);
        holder.downvoteImg.setSelected(isDownvoted);

        // 2. Upvote Click
        holder.upvoteImg.setOnClickListener(v -> {
            toggleVote(postsRef, "upvotes", "downvotes", userId, post);
        });

        // 3. Downvote Click
        holder.downvoteImg.setOnClickListener(v -> {
            toggleVote(postsRef, "downvotes", "upvotes", userId, post);
        });

        // --- Bookmark Logic (FIXED) ---
        if (post.getPostId() != null) {
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<String> bookmarks = (List<String>) task.getResult().get("bookmarks");
                    boolean isBookmarked = (bookmarks != null && bookmarks.contains(post.getPostId()));
                    holder.bookmarkBtn.setSelected(isBookmarked);
                    holder.bookmarkBtn.refreshDrawableState();
                }
            });

            holder.bookmarkBtn.setOnClickListener(v -> {
                boolean newState = !holder.bookmarkBtn.isSelected();
                holder.bookmarkBtn.setSelected(newState);
                holder.bookmarkBtn.refreshDrawableState();

                if (!newState) {
                    userRef.update("bookmarks", FieldValue.arrayRemove(post.getPostId()));
                    Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
                } else {
                    userRef.update("bookmarks", FieldValue.arrayUnion(post.getPostId()));
                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
                }
            });
        }

        holder.commentImg.setOnClickListener(v -> {
            context.startActivity(new Intent(context, PostDetailsActivity.class).putExtra("POST_ID", post.getPostId()));
        });

        holder.moreBtn.setOnClickListener(v -> showPopup(v, post, userId));
    }

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

    private void showPopup(View v, Post p, String id) {
        PopupMenu popup = new PopupMenu(context, v);
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
}