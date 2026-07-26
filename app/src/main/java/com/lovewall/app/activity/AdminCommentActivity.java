package com.lovewall.app.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.*;
import com.lovewall.app.R;
import com.lovewall.app.api.ApiClient;
import com.lovewall.app.utils.Prefs;
import com.lovewall.app.utils.ToastUtil;

public class AdminCommentActivity extends AppCompatActivity {
    private Prefs prefs;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_comments);
        prefs = new Prefs(this);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        container = findViewById(R.id.commentsContainer);
        loadComments();
    }

    private void loadComments() {
        new Thread(() -> {
            try {
                String resp = ApiClient.get("/api/admin/comments?limit=50", prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                JsonArray arr = json.getAsJsonArray("comments");
                runOnUiThread(() -> {
                    container.removeAllViews();
                    if (arr.size() == 0) {
                        TextView tv = new TextView(this);
                        tv.setText("暂无评论");
                        tv.setTextColor(getResources().getColor(R.color.gray));
                        tv.setPadding(32, 32, 32, 32);
                        container.addView(tv);
                        return;
                    }
                    for (JsonElement el : arr) {
                        JsonObject c = el.getAsJsonObject();
                        View item = getLayoutInflater().inflate(R.layout.item_admin_comment, container, false);
                        ((TextView) item.findViewById(R.id.tvAuthor)).setText(c.has("author_name") ? c.get("author_name").getAsString() : "");
                        ((TextView) item.findViewById(R.id.tvContent)).setText(c.has("content") ? c.get("content").getAsString() : "");
                        ((TextView) item.findViewById(R.id.tvPostTitle)).setText(c.has("post_title") ? "帖子: " + c.get("post_title").getAsString() : "");
                        String id = c.get("id").getAsString();
                        item.findViewById(R.id.btnDelete).setOnClickListener(v -> deleteComment(id));
                        container.addView(item);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "加载失败"));
            }
        }).start();
    }

    private void deleteComment(String id) {
        new android.app.AlertDialog.Builder(this).setTitle("删除评论？")
            .setPositiveButton("删除", (d, w) -> {
                new Thread(() -> {
                    try {
                        ApiClient.delete("/api/admin/comments/" + id, prefs.getToken());
                        runOnUiThread(() -> { ToastUtil.show(this, "已删除"); loadComments(); });
                    } catch (Exception e) { runOnUiThread(() -> ToastUtil.show(this, "删除失败")); }
                }).start();
            }).setNegativeButton("取消", null).show();
    }
}
