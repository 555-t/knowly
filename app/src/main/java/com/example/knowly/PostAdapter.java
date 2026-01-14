package com.example.knowly;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> postList;
    // Temporary ID for testing. Later use FirebaseAuth.getInstance().getUid()
    private String currentUserId = "test_user_777";

    public PostAdapter(List<Post> postList) { this.postList = postList; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);

        // 1. Set text views
        holder.content.setText(post.getContent());
        holder.author.setText(post.getAuthor());

        // 2. Set the numbers (These now use the .size() logic from your Post class)
        holder.upvoteNum.setText(String.valueOf(post.getUpvote_num()));
        holder.downvoteNum.setText(String.valueOf(post.getDownvote_num()));
        holder.commentNum.setText(String.valueOf(post.getComment_num()));

        // 3. Category logic
        if (post.getCategories() != null && !post.getCategories().isEmpty()) {
            holder.category.setText(post.getCategories().get(0));
            holder.category.setVisibility(View.VISIBLE);
        } else {
            holder.category.setVisibility(View.GONE);
        }

        // 4. Visual Feedback: Change arrow color if user has already voted
        if (post.getUpvotes() != null && post.getUpvotes().containsKey(currentUserId)) {
            holder.upvoteImg.setColorFilter(Color.parseColor("#3498db")); // Blue
        } else {
            holder.upvoteImg.setColorFilter(Color.GRAY);
        }

        if (post.getDownvotes() != null && post.getDownvotes().containsKey(currentUserId)) {
            holder.downvoteImg.setColorFilter(Color.parseColor("#e74c3c")); // Red
        } else {
            holder.downvoteImg.setColorFilter(Color.GRAY);
        }

        // 5. Firebase Toggle Logic
        if (post.getPostId() != null) {
            DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(post.getPostId());

            // UPVOTE CLICK
            holder.upvoteImg.setOnClickListener(v -> {
                int currentPos = holder.getAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) return;

                Post currentPost = postList.get(currentPos);
                DatabaseReference upRef = postRef.child("upvotes").child(currentUserId);
                DatabaseReference downRef = postRef.child("downvotes").child(currentUserId);

                if (currentPost.getUpvotes() != null && currentPost.getUpvotes().containsKey(currentUserId)) {
                    upRef.removeValue(); // Already upvoted? Remove it (toggle off)
                } else {
                    upRef.setValue(true); // Add upvote
                    downRef.removeValue(); // Remove downvote if it exists
                }
            });

            // DOWNVOTE CLICK
            holder.downvoteImg.setOnClickListener(v -> {
                int currentPos = holder.getAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) return;

                Post currentPost = postList.get(currentPos);
                DatabaseReference upRef = postRef.child("upvotes").child(currentUserId);
                DatabaseReference downRef = postRef.child("downvotes").child(currentUserId);

                if (currentPost.getDownvotes() != null && currentPost.getDownvotes().containsKey(currentUserId)) {
                    downRef.removeValue(); // Toggle off
                } else {
                    downRef.setValue(true); // Add downvote
                    upRef.removeValue(); // Remove upvote if it exists
                }
            });
        }
    }

    @Override
    public int getItemCount() { return postList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView content, author, category;
        TextView upvoteNum, downvoteNum, commentNum;
        ImageView upvoteImg, downvoteImg; // Added these for easier access

        public ViewHolder(View v) {
            super(v);
            content = v.findViewById(R.id.post_content);
            author = v.findViewById(R.id.username);
            category = v.findViewById(R.id.category_text);
            upvoteNum = v.findViewById(R.id.upvote_num);
            downvoteNum = v.findViewById(R.id.downvote_num);
            commentNum = v.findViewById(R.id.comment_num);

            // Find the image views for colors and clicks
            upvoteImg = v.findViewById(R.id.upvote_img);
            downvoteImg = v.findViewById(R.id.downvote_img);
        }
    }
}