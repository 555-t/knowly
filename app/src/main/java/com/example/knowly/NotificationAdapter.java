package com.example.knowly;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationItem> list;

    public NotificationAdapter(List<NotificationItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This connects to your item_notification.xml layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = list.get(position);

        holder.username.setText(item.getUsername());
        holder.action.setText(item.getAction());

        // --- UPDATED TIME LOGIC ---
        if (item.getTimestamp() != null) {
            long milliseconds = item.getTimestamp().toDate().getTime();
            holder.time.setText(getTimeAgo(milliseconds));
        } else {
            holder.time.setText("Just now");
        }

        // --- ICON LOGIC ---
        String iconName = item.getIconRes();
        int resId = 0;
        if (iconName != null && !iconName.isEmpty()) {
            resId = holder.itemView.getContext().getResources().getIdentifier(
                    iconName, "drawable", holder.itemView.getContext().getPackageName());
        }

        if (resId != 0) {
            holder.icon.setImageResource(resId);
        } else {
            holder.icon.setImageResource(R.drawable.default_icon);
        }
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, action, time;
        ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // These IDs must match your item_notification.xml exactly
            username = itemView.findViewById(R.id.user);
            action = itemView.findViewById(R.id.notif_info);
            time = itemView.findViewById(R.id.notif_time);
            icon = itemView.findViewById(R.id.notif_icon);
        }
    }

    public String getTimeAgo(long time) {
        return android.text.format.DateUtils.getRelativeTimeSpanString(
                time,
                System.currentTimeMillis(),
                android.text.format.DateUtils.MINUTE_IN_MILLIS
        ).toString();
    }
}