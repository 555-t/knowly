package com.example.knowly;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
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

        // 1. Setup Time
        String timeAgo = (item.getTimestamp() != 0) ? getTimeAgo(item.getTimestamp()) : "Just now";

        // 2. Identify the Views based on ViewHolder type
        TextView usernameTv, actionTv, timeTv;
        if (holder instanceof CommentViewHolder) {
            CommentViewHolder h = (CommentViewHolder) holder;
            usernameTv = h.username;
            actionTv = h.action;
            timeTv = h.time;
        } else {
            FollowViewHolder h = (FollowViewHolder) holder;
            usernameTv = h.username;
            actionTv = h.action;
            timeTv = h.time;
        }

        // 3. SET TAG & PLACEHOLDERS (Prevents Recycling Bugs)
        // We use the timestamp as a unique ID for this specific data binding
        String uniqueId = String.valueOf(item.getTimestamp());
        usernameTv.setTag(uniqueId);

        actionTv.setText(item.getAction());
        timeTv.setText(timeAgo);

        // 4. FETCH REAL USERNAME LOGIC
        String storedUsername = item.getUsername();

        if (storedUsername != null && storedUsername.length() > 20) {
            // It's a UID! We need to fetch the real name
            usernameTv.setText("..."); // Temporary placeholder
            fetchNameFromFirebase(storedUsername, usernameTv, uniqueId);
        } else {
            // It's already a name, just set it
            usernameTv.setText(storedUsername != null ? storedUsername : "User");
        }
    }

    private void fetchNameFromFirebase(String uid, TextView textView, String tag) {
        FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .child("username")
                .get()
                .addOnCompleteListener(task -> {
                    // CRITICAL: Only update the UI if the view is still
                    // supposed to show this specific notification
                    if (textView.getTag() != null && textView.getTag().equals(tag)) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            textView.setText(String.valueOf(task.getResult().getValue()));
                        } else {
                            textView.setText("Unknown User");
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // --- ViewHolders remain the same ---
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView username, action, time;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.user);
            action = itemView.findViewById(R.id.notif_info);
            time = itemView.findViewById(R.id.time_commented);
        }
    }

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