package com.example.knowly;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
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
        holder.userName.setText(comment.getAuthorId()); // You can map this to a real name later

        // Convert timestamp to "2h ago" format
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                comment.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS);
        holder.commentTime.setText(timeAgo);

        // Set the first letter for the profile circle
        if (comment.getAuthorId() != null && !comment.getAuthorId().isEmpty()) {
            holder.profileInitial.setText(comment.getAuthorId().substring(0, 1).toUpperCase());
        }
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