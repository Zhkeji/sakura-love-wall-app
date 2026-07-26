package com.lovewall.app.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.*;
import com.lovewall.app.R;
import com.lovewall.app.adapter.MessageAdapter;
import com.lovewall.app.api.ApiClient;
import com.lovewall.app.model.Message;
import com.lovewall.app.utils.Prefs;
import com.lovewall.app.utils.ToastUtil;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private String conversationId;
    private Prefs prefs;
    private RecyclerView rv;
    private MessageAdapter adapter;
    private List<Message> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        prefs = new Prefs(this);
        conversationId = getIntent().getStringExtra("conversationId");
        String otherName = getIntent().getStringExtra("otherName");

        ((TextView) findViewById(R.id.tvName)).setText(otherName);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rv = findViewById(R.id.recyclerMessages);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(messages, prefs.getUserId());
        rv.setAdapter(adapter);

        EditText etMsg = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> {
            String content = etMsg.getText().toString().trim();
            if (content.isEmpty()) return;
            sendMessage(content, etMsg);
        });

        loadMessages();
    }

    private void loadMessages() {
        new Thread(() -> {
            try {
                String resp = ApiClient.get("/api/chat/conversations/" + conversationId + "/messages", prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                JsonArray arr = json.getAsJsonArray("messages");
                Gson gson = new Gson();
                List<Message> list = new ArrayList<>();
                for (JsonElement el : arr) list.add(gson.fromJson(el, Message.class));

                runOnUiThread(() -> {
                    messages.clear();
                    messages.addAll(list);
                    adapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) rv.scrollToPosition(messages.size() - 1);
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "加载消息失败"));
            }
        }).start();
    }

    private void sendMessage(String content, EditText et) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("content", content);
                String resp = ApiClient.post("/api/chat/conversations/" + conversationId + "/messages", body.toString(), prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                Gson gson = new Gson();
                Message msg = gson.fromJson(json.getAsJsonObject("message"), Message.class);

                runOnUiThread(() -> {
                    messages.add(msg);
                    adapter.notifyItemInserted(messages.size() - 1);
                    rv.scrollToPosition(messages.size() - 1);
                    et.setText("");
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "发送失败"));
            }
        }).start();
    }
}
