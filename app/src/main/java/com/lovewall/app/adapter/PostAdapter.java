package com.lovewall.app.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.lovewall.app.R;
import com.lovewall.app.model.Post;
import com.lovewall.app.utils.TimeUtil;
import java.util.*;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    public interface OnPostClickListener {
        void onPostClick(Post post);
        void onLikeClick(Post post, int position);
        void onExpandClick(Post post, int position);
        void onBookmarkClick(Post post, int position);
        void onShareClick(Post post);
    }

    private final List<Post> posts;
    private final OnPostClickListener listener;
    private final Set<Integer> expandedPositions = new HashSet<>();

    public PostAdapter(List<Post> posts, OnPostClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    public void toggleExpand(int position) {
        if (expandedPositions.contains(position)) expandedPositions.remove(position);
        else expandedPositions.add(position);
        notifyItemChanged(position);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Post p = posts.get(position);

        // 置顶标识
        if (p.is_pinned == 1) {
            h.tvPinned.setVisibility(View.VISIBLE);
        } else {
            h.tvPinned.setVisibility(View.GONE);
        }

        h.tvTitle.setText(p.title);

        // 内容折叠/展开
        boolean isExpanded = expandedPositions.contains(position);
        boolean isLong = p.content != null && p.content.length() > 150;
        if (isLong && !isExpanded) {
            h.tvContent.setText(p.content.substring(0, 150) + "...");
            h.tvExpand.setVisibility(View.VISIBLE);
            h.tvExpand.setText("展开全文");
            h.tvExpand.setOnClickListener(v -> listener.onExpandClick(p, position));
        } else {
            h.tvContent.setText(p.content);
            h.tvExpand.setVisibility(isLong ? View.VISIBLE : View.GONE);
            h.tvExpand.setText("收起");
            h.tvExpand.setOnClickListener(v -> listener.onExpandClick(p, position));
        }

        h.tvAuthor.setText(p.author_name != null ? p.author_name : "匿名用户");
        h.tvTime.setText(TimeUtil.getTimeAgo(p.created_at));

        // 分类
        if (p.category != null && !p.category.isEmpty()) {
            h.tvCategory.setVisibility(View.VISIBLE);
            switch (p.category) {
                case "confession": h.tvCategory.setText("💕 表白"); break;
                case "daily": h.tvCategory.setText("☕ 日常"); break;
                case "expand": h.tvCategory.setText("🤝 扩列"); break;
                default: h.tvCategory.setText("💕 表白");
            }
        } else {
            h.tvCategory.setVisibility(View.GONE);
        }

        // 标签
        List<String> tags = p.getTagList();
        if (!tags.isEmpty()) {
            h.tvTags.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            for (String t : tags) sb.append("#").append(t).append("  ");
            h.tvTags.setText(sb.toString());
        } else {
            h.tvTags.setVisibility(View.GONE);
        }

        // 点赞
        h.btnLike.setText((p.isLiked ? "❤️ " : "🤍 ") + p.likes);
        h.btnLike.setTextColor(p.isLiked ? Color.parseColor("#e74c6f") : Color.parseColor("#999999"));
        h.btnLike.setOnClickListener(v -> listener.onLikeClick(p, position));

        // 评论
        h.tvComments.setText("💬 " + p.comments_count);

        // 浏览量
        h.tvViews.setText("👁 " + (p.views != null ? p.views : 0));

        // 收藏
        h.btnBookmark.setText(p.isBookmarked ? "🔖" : "🏷️");
        h.btnBookmark.setOnClickListener(v -> listener.onBookmarkClick(p, position));

        // 分享
        h.btnShare.setOnClickListener(v -> listener.onShareClick(p));

        h.itemView.setOnClickListener(v -> listener.onPostClick(p));
    }

    @Override
    public int getItemCount() { return posts.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPinned, tvTitle, tvContent, tvExpand, tvAuthor, tvTime, tvCategory, tvTags;
        TextView btnLike, tvComments, tvViews, btnBookmark, btnShare;
        ViewHolder(View v) {
            super(v);
            tvPinned = v.findViewById(R.id.tvPinned);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvContent = v.findViewById(R.id.tvContent);
            tvExpand = v.findViewById(R.id.tvExpand);
            tvAuthor = v.findViewById(R.id.tvAuthor);
            tvTime = v.findViewById(R.id.tvTime);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvTags = v.findViewById(R.id.tvTags);
            btnLike = v.findViewById(R.id.btnLike);
            tvComments = v.findViewById(R.id.tvComments);
            tvViews = v.findViewById(R.id.tvViews);
            btnBookmark = v.findViewById(R.id.btnBookmark);
            btnShare = v.findViewById(R.id.btnShare);
        }
    }
}
