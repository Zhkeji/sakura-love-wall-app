package com.lovewall.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
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

public class ChatListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        Prefs prefs = new Prefs(this);

        RecyclerView rv = findViewById(R.id.recyclerConversations);
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        new Thread(() -> {
            try {
                String resp = ApiClient.get("/api/chat/conversations", prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                JsonArray arr = json.getAsJsonArray("conversations");
                List<Conversation> list = new ArrayList<>();
                Gson gson = new Gson();
                for (JsonElement el : arr) list.add(gson.fromJson(el, Conversation.class));

                runOnUiThread(() -> {
                    if (list.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    } else {
                        ConversationAdapter adapter = new ConversationAdapter(list, conv -> {
                            Intent intent = new Intent(this, ChatActivity.class);
                            intent.putExtra("conversationId", conv.id);
                            intent.putExtra("otherName", conv.other_name);
                            startActivity(intent);
                        });
                        rv.setAdapter(adapter);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "加载失败"));
            }
        }).start();
    }
}
