package com.lovewall.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.lovewall.app.R;
import com.lovewall.app.model.Message;
import com.lovewall.app.utils.TimeUtil;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private final List<Message> messages;
    private final String myId;

    public MessageAdapter(List<Message> messages, String myId) {
        this.messages = messages;
        this.myId = myId;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Message m = messages.get(position);
        boolean isSent = m.sender_id != null && m.sender_id.equals(myId);

        if (isSent) {
            h.layoutSent.setVisibility(View.VISIBLE);
            h.layoutReceived.setVisibility(View.GONE);
            h.tvMsg.setText(m.content);
            h.tvTime.setText(TimeUtil.getTimeAgo(m.created_at));
        } else {
            h.layoutSent.setVisibility(View.GONE);
            h.layoutReceived.setVisibility(View.VISIBLE);
            h.tvMsgRecv.setText(m.content);
            h.tvTimeRecv.setText(TimeUtil.getTimeAgo(m.created_at));
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutSent, layoutReceived;
        TextView tvMsg, tvTime, tvMsgRecv, tvTimeRecv;
        ViewHolder(View v) {
            super(v);
            layoutSent = v.findViewById(R.id.layoutSent);
            layoutReceived = v.findViewById(R.id.layoutReceived);
            tvMsg = v.findViewById(R.id.tvMsg);
            tvTime = v.findViewById(R.id.tvTime);
            tvMsgRecv = v.findViewById(R.id.tvMsgRecv);
            tvTimeRecv = v.findViewById(R.id.tvTimeRecv);
        }
    }
}
