package com.lovewall.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.lovewall.app.R;
import com.lovewall.app.model.Conversation;
import com.lovewall.app.utils.TimeUtil;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    public interface OnConvClick { void onClick(Conversation conv); }

    private final List<Conversation> list;
    private final OnConvClick listener;

    public ConversationAdapter(List<Conversation> list, OnConvClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Conversation c = list.get(position);
        h.tvName.setText(c.other_name != null ? c.other_name : "用户");
        h.tvLastMsg.setText(c.last_message != null ? c.last_message : "暂无消息");
        h.tvTime.setText(c.last_message_at != null ? TimeUtil.getTimeAgo(c.last_message_at) : "");
        h.unreadDot.setVisibility(c.unread_count > 0 ? View.VISIBLE : View.GONE);
        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMsg, tvTime;
        View unreadDot;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvLastMsg = v.findViewById(R.id.tvLastMsg);
            tvTime = v.findViewById(R.id.tvTime);
            unreadDot = v.findViewById(R.id.unreadDot);
        }
    }
}
