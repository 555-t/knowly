package com.example.knowly;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<NotificationItem> list;
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

        // 1. Setup Time using helper
        String timeAgo = (item.getTimestamp() != null) ? getTimeAgo(item.getTimestampMillis()) : "Just now";

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

        // 2. Set Tag for verification during async load
        usernameTv.setTag(item.getFromUserId());

        actionTv.setText(item.getAction());
        timeTv.setText(timeAgo);

        // 3. FETCH REAL USERNAME FROM FIRESTORE
        String fromUid = item.getFromUserId();
        if (fromUid != null) {
            usernameTv.setText("..."); // Placeholder
            fetchNameFromFirestore(fromUid, usernameTv);
        }
    }

    private void fetchNameFromFirestore(String uid, TextView textView) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Check if the TextView is still intended for this UID (prevents recycling bugs)
                    if (textView.getTag() != null && textView.getTag().equals(uid)) {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("username");
                            textView.setText(name != null ? name : "User");
                        } else {
                            textView.setText("Unknown User");
                        }
                    }
                });
    }

    @Override
    public int getItemCount() { return list.size(); }

    // --- ViewHolders ---
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