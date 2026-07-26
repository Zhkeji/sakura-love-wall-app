package com.lovewall.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.lovewall.app.R;
import com.lovewall.app.model.Post;
import com.lovewall.app.utils.TimeUtil;
import java.util.List;

public class AdminPostAdapter extends RecyclerView.Adapter<AdminPostAdapter.ViewHolder> {
    public interface OnActionListener {
        void onApprove(String id);
        void onHide(String id);
        void onDelete(String id);
    }

    private final List<Post> posts;
    private final OnActionListener listener;

    public AdminPostAdapter(List<Post> posts, OnActionListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Post p = posts.get(position);
        h.tvTitle.setText(p.title);
        h.tvAuthor.setText(p.author_name != null ? p.author_name : "匿名");
        h.tvTime.setText(TimeUtil.getTimeAgo(p.created_at));
        h.tvStatus.setText(p.status);

        // 状态颜色
        switch (p.status) {
            case "published": h.tvStatus.setBackgroundResource(R.color.green); break;
            case "pending": h.tvStatus.setBackgroundResource(R.color.orange); break;
            case "hidden": h.tvStatus.setBackgroundResource(R.color.gray); break;
        }

        // 按钮
        h.btnApprove.setVisibility("pending".equals(p.status) ? View.VISIBLE : View.GONE);
        h.btnHide.setVisibility("published".equals(p.status) ? View.VISIBLE : View.GONE);

        h.btnApprove.setOnClickListener(v -> listener.onApprove(p.id));
        h.btnHide.setOnClickListener(v -> listener.onHide(p.id));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(p.id));
    }

    @Override
    public int getItemCount() { return posts.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvTime, tvStatus;
        Button btnApprove, btnHide, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvAuthor = v.findViewById(R.id.tvAuthor);
            tvTime = v.findViewById(R.id.tvTime);
            tvStatus = v.findViewById(R.id.tvStatus);
            btnApprove = v.findViewById(R.id.btnApprove);
            btnHide = v.findViewById(R.id.btnHide);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
