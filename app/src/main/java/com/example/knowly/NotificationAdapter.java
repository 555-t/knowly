package com.example.knowly;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<NotificationItem> list;
    private static final int TYPE_COMMENT = 1;
    private static final int TYPE_FOLLOW = 2;
    private static final int TYPE_UPVOTE = 3; // Add this

    public NotificationAdapter(List<NotificationItem> list) {
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        String type = list.get(position).getType();

        // Debugging: This will show up in your Logcat (filter by "NotifType")
        android.util.Log.d("NotifType", "Notification type found: [" + type + "]");

        if (type != null) {
            String cleanType = type.trim().toLowerCase(); // Removes spaces and ignores Case
            if (cleanType.equals("follow")) return TYPE_FOLLOW;
            if (cleanType.equals("upvote")) return TYPE_UPVOTE;
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
            // Both Upvote and Comment use this layout, we just change the icon in onBind
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_notif, parent, false);
            return new CommentViewHolder(view);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationItem item = list.get(position);
        String timeAgo = (item.getTimestamp() != null) ? getTimeAgo(item.getTimestampMillis()) : "Just now";

        TextView usernameTv, actionTv, timeTv;

        if (holder instanceof FollowViewHolder) {
            // --- LOGIC FOR FOLLOW ---
            FollowViewHolder h = (FollowViewHolder) holder;
            usernameTv = h.username;
            actionTv = h.action; // This is @id/notif_info2
            timeTv = h.time;
        } else {
            // --- LOGIC FOR COMMENT / UPVOTE ---
            CommentViewHolder h = (CommentViewHolder) holder;
            usernameTv = h.username;
            actionTv = h.action; // This is @id/notif_info
            timeTv = h.time;

            ImageView iconIv = h.itemView.findViewById(R.id.imageView5);
            if ("upvote".equals(item.getType())) {
                iconIv.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                iconIv.setImageResource(android.R.drawable.stat_notify_chat);
            }
        }

        usernameTv.setTag(item.getFromUserId());

        // This line MUST be outside the if/else to apply to both types!
        if (item.getAction() != null) {
            actionTv.setText(item.getAction());
        } else {
            actionTv.setText("interacted with you");
        }

        timeTv.setText(timeAgo);

        String fromUid = item.getFromUserId();
        if (fromUid != null) {
            usernameTv.setText("...");
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