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
        // This inflates the individual row layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = list.get(position);

        // Setting the text and images for each row
        holder.user.setText(item.getUsername());
        holder.info.setText(item.getAction());
        holder.time.setText(item.getTime());
        holder.icon.setImageResource(item.getIconRes());
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView user, info, time;
        ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            // These IDs must match the ones in your item_notification.xml
            user = itemView.findViewById(R.id.user_name);
            info = itemView.findViewById(R.id.notif_description);
            time = itemView.findViewById(R.id.notif_time);
            icon = itemView.findViewById(R.id.notif_icon);
        }
    }
}