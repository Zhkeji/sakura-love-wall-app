package com.lovewall.app.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.*;
import com.lovewall.app.R;
import com.lovewall.app.adapter.ConversationAdapter;
import com.lovewall.app.api.ApiClient;
import com.lovewall.app.model.Conversation;
import com.lovewall.app.utils.Prefs;
import com.lovewall.app.utils.ToastUtil;
import java.util.ArrayList;
import java.util.List;

// 管理员私信管理
public class AdminChatActivity extends AppCompatActivity {
    private Prefs prefs;
    private RecyclerView rv;
    private ConversationAdapter adapter;
    private List<Conversation> conversations = new ArrayList<>();
    private LinearLayout chatDetail;
    private LinearLayout messagesContainer;
    private String currentConvId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chats);
        prefs = new Prefs(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        rv = findViewById(R.id.recyclerAdminChats);
        rv.setLayoutManager(new LinearLayoutManager(this));
        chatDetail = findViewById(R.id.chatDetail);
        messagesContainer = findViewById(R.id.messagesContainer);

        adapter = new ConversationAdapter(conversations, conv -> {
            currentConvId = conv.id;
            loadMessages(conv.id);
        });
        rv.setAdapter(adapter);

        // 介入发送
        EditText etIntervene = findViewById(R.id.etIntervene);
        findViewById(R.id.btnIntervene).setOnClickListener(v -> {
            String content = etIntervene.getText().toString().trim();
            if (content.isEmpty() || currentConvId == null) return;
            sendIntervene(content, etIntervene);
        });

        loadConversations();
    }

    private void loadConversations() {
        new Thread(() -> {
            try {
                String resp = ApiClient.get("/api/chat/admin/conversations", prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                JsonArray arr = json.getAsJsonArray("conversations");
                Gson gson = new Gson();
                conversations.clear();
                for (JsonElement el : arr) {
                    Conversation c = gson.fromJson(el, Conversation.class);
                    // 从 user1_name/user2_name 设置 other_name
                    JsonObject obj = el.getAsJsonObject();
                    c.other_name = (obj.has("user1_name") ? obj.get("user1_name").getAsString() : "") + " ↔ " +
                                   (obj.has("user2_name") ? obj.get("user2_name").getAsString() : "");
                    conversations.add(c);
                }
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "加载失败"));
            }
        }).start();
    }

    private void loadMessages(String convId) {
        new Thread(() -> {
            try {
                String resp = ApiClient.get("/api/chat/admin/conversations/" + convId + "/messages", prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                JsonArray arr = json.getAsJsonArray("messages");
                Gson gson = new Gson();

                runOnUiThread(() -> {
                    chatDetail.setVisibility(View.VISIBLE);
                    messagesContainer.removeAllViews();

                    for (JsonElement el : arr) {
                        Message msg = gson.fromJson(el, Message.class);
                        TextView tv = new TextView(this);
                        if ("system".equals(msg.type)) {
                            tv.setText("🔒 " + msg.content);
                            tv.setTextSize(12);
                            tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                            tv.setPadding(16, 8, 16, 8);
                        } else {
                            tv.setText(msg.sender_name + ": " + msg.content);
                            tv.setTextSize(14);
                            tv.setTextColor(getResources().getColor(R.color.black));
                            tv.setPadding(16, 8, 16, 8);
                        }
                        tv.setBackgroundResource(R.drawable.bg_input);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 8);
                        tv.setLayoutParams(params);
                        messagesContainer.addView(tv);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "加载消息失败"));
            }
        }).start();
    }

    private void sendIntervene(String content, EditText et) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("content", content);
                ApiClient.post("/api/chat/admin/conversations/" + currentConvId + "/intervene",
                    body.toString(), prefs.getToken());
                runOnUiThread(() -> {
                    et.setText("");
                    ToastUtil.show(this, "已介入");
                    loadMessages(currentConvId);
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "介入失败"));
            }
        }).start();
    }

    // 内部消息类
    static class Message {
        String id, sender_id, content, type, sender_name;
    }
}
