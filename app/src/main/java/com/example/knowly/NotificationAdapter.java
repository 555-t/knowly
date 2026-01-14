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
        holder.time.setText(item.getTime());

        // --- IMPROVED ICON LOGIC ---
        String iconName = item.getIconRes();
        int resId = 0;

        if (iconName != null && !iconName.isEmpty()) {
            // Find the image ID by its string name
            resId = holder.itemView.getContext().getResources().getIdentifier(
                    iconName,
                    "drawable",
                    holder.itemView.getContext().getPackageName()
            );
        }

        // Use the icon from Firebase if found, otherwise use your new default_icon.xml
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
}