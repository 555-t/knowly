package com.example.knowly;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<NotificationItem> list;

    // View Type constants for choosing layouts
    private static final int TYPE_COMMENT = 1;
    private static final int TYPE_FOLLOW = 2;

    public NotificationAdapter(List<NotificationItem> list) {
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        // Decide layout based on the "type" field
        if (list.get(position).getType() != null && list.get(position).getType().equals("follow")) {
            return TYPE_FOLLOW;
        }
        return TYPE_COMMENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOLLOW) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.following_notif, parent, false);
            return new FollowViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_notif, parent, false);
            return new CommentViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationItem item = list.get(position);

        // FIX: Compare long to 0, not null. Remove .toDate() as it is now a direct millisecond value.
        String timeAgo;
        if (item.getTimestamp() != 0) {
            timeAgo = getTimeAgo(item.getTimestamp());
        } else {
            timeAgo = "Just now";
        }

        if (holder instanceof CommentViewHolder) {
            CommentViewHolder h = (CommentViewHolder) holder;
            h.username.setText(item.getUsername());
            h.action.setText(item.getAction());
            h.time.setText(timeAgo);
        } else if (holder instanceof FollowViewHolder) {
            FollowViewHolder h = (FollowViewHolder) holder;
            h.username.setText(item.getUsername());
            h.action.setText(item.getAction());
            h.time.setText(timeAgo);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // --- ViewHolder for comment_notif.xml ---
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView username, action, time;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.user);
            action = itemView.findViewById(R.id.notif_info);
            time = itemView.findViewById(R.id.time_commented);
        }
    }

    // --- ViewHolder for following_notif.xml ---
    public static class FollowViewHolder extends RecyclerView.ViewHolder {
        TextView username, action, time;

        public FollowViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.user2);
            action = itemView.findViewById(R.id.notif_info2);
            time = itemView.findViewById(R.id.time_commented2);
        }
    }

    public String getTimeAgo(long time) {
        return DateUtils.getRelativeTimeSpanString(
                time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        ).toString();
    }
}