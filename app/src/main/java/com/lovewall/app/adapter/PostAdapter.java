package com.lovewall.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.lovewall.app.R;
import com.lovewall.app.model.Post;
import com.lovewall.app.utils.TimeUtil;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    public interface OnPostClick { void onClick(Post post); }

    private final List<Post> posts;
    private final OnPostClick listener;

    public PostAdapter(List<Post> posts, OnPostClick listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Post p = posts.get(position);
        h.tvTitle.setText(p.title);
        h.tvContent.setText(p.content);
        h.tvAuthor.setText((p.author_name != null ? p.author_name : "匿名用户") + " · " + TimeUtil.getTimeAgo(p.created_at));
        h.tvLikes.setText((p.isLiked ? "❤️ " : "🤍 ") + p.likes);
        h.tvComments.setText("💬 " + p.comments_count);

        List<String> tags = p.getTagList();
        if (!tags.isEmpty()) {
            h.tvTags.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            for (String t : tags) sb.append("#").append(t).append("  ");
            h.tvTags.setText(sb.toString());
        } else {
            h.tvTags.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    @Override
    public int getItemCount() { return posts.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvAuthor, tvLikes, tvComments, tvTags;
        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvContent = v.findViewById(R.id.tvContent);
            tvAuthor = v.findViewById(R.id.tvAuthor);
            tvLikes = v.findViewById(R.id.tvLikes);
            tvComments = v.findViewById(R.id.tvComments);
            tvTags = v.findViewById(R.id.tvTags);
        }
    }
}
