package com.example.knowly2;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.VH> {

    private final List<Post> posts;
    private final Context ctx;

    public PostAdapter(Context ctx, List<Post> posts) {
        this.ctx = ctx;
        this.posts = posts;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Post p = posts.get(pos);

        // Basic text
        h.username.setText(p.username);
        h.time.setText(p.time);
        h.content.setText(p.content);
        h.tag1.setText(p.tag1);
        h.tag2.setText(p.tag2);

        // Hide tag2 if empty
        if (p.tag2 == null || p.tag2.trim().isEmpty()) {
            h.tag2.setVisibility(View.GONE);
        } else {
            h.tag2.setVisibility(View.VISIBLE);
        }

        // Avatar letter
        String first = p.username.length() > 0 ? ("" + Character.toUpperCase(p.username.charAt(0))) : "U";
        h.avatar.setText(first);

        // Counts
        h.likeCount.setText(String.valueOf(p.likeCount));
        h.dislikeCount.setText(String.valueOf(p.dislikeCount));
        h.commentCount.setText(String.valueOf(p.commentCount));

        // Save icon state
        updateSaveIcon(h, p.isSaved);

        // Click Like
        h.btnLike.setOnClickListener(v -> {
            p.likeCount++;
            notifyItemChanged(pos);
        });

        // Click Dislike
        h.btnDislike.setOnClickListener(v -> {
            p.dislikeCount++;
            notifyItemChanged(pos);
        });

        // Click Comment
        h.btnComment.setOnClickListener(v ->
                Toast.makeText(ctx, "Open comments (later)", Toast.LENGTH_SHORT).show()
        );

        // Click Save toggle
        h.btnSave.setOnClickListener(v -> {
            p.isSaved = !p.isSaved;
            updateSaveIcon(h, p.isSaved);
            Toast.makeText(ctx, p.isSaved ? "Saved" : "Unsaved", Toast.LENGTH_SHORT).show();
        });

        // 3 dots menu: Block / Report
        h.btnMore.setOnClickListener(v -> showMoreMenu(v, p));
    }

    private void updateSaveIcon(VH h, boolean saved) {
        if (saved) {
            h.btnSave.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            h.btnSave.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    private void showMoreMenu(View anchor, Post p) {
        PopupMenu popup = new PopupMenu(ctx, anchor);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_post_more, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_block) {
                Toast.makeText(ctx, "Blocked " + p.username, Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_report) {
                Toast.makeText(ctx, "Reported post", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popup.show();
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView avatar, username, time, content, tag1, tag2;
        TextView likeCount, dislikeCount, commentCount;

        View btnLike, btnDislike, btnComment;
        ImageView btnSave, btnMore;

        VH(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.tvAvatar);
            username = itemView.findViewById(R.id.tvUsername);
            time = itemView.findViewById(R.id.tvTime);
            content = itemView.findViewById(R.id.tvContent);
            tag1 = itemView.findViewById(R.id.tag1);
            tag2 = itemView.findViewById(R.id.tag2);

            btnMore = itemView.findViewById(R.id.btnMore);

            btnLike = itemView.findViewById(R.id.btnLike);
            btnDislike = itemView.findViewById(R.id.btnDislike);
            btnComment = itemView.findViewById(R.id.btnComment);

            likeCount = itemView.findViewById(R.id.tvLikeCount);
            dislikeCount = itemView.findViewById(R.id.tvDislikeCount);
            commentCount = itemView.findViewById(R.id.tvCommentCount);

            btnSave = itemView.findViewById(R.id.btnSave);
        }
    }
}
