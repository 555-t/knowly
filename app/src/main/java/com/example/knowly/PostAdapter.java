package com.example.knowly;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> postList;

    public PostAdapter(List<Post> postList) { this.postList = postList; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This connects to item_post.xml which I see in your layout folder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.content.setText(post.getContent());
        holder.author.setText(post.getAuthor());

        // Check if there are categories and show the first one
        if (post.getCategories() != null && !post.getCategories().isEmpty()) {
            holder.category.setText(post.getCategories().get(0));
            holder.category.setVisibility(View.VISIBLE);
        } else {
            holder.category.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return postList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView content, author;
        // Note: You used a TextView for categories in your XML, not a ChipGroup
        TextView category;

        public ViewHolder(View v) {
            super(v);
            // Matching your item_post.xml IDs exactly
            content = v.findViewById(R.id.post_content); // Fixed from postContent
            author = v.findViewById(R.id.username);      // Fixed from postAuthor
            category = v.findViewById(R.id.category_text); // Fixed from postChips
        }
    }
}