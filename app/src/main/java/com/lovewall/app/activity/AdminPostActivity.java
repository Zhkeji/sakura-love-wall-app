package com.lovewall.app.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.*;
import com.lovewall.app.R;
import com.lovewall.app.adapter.AdminPostAdapter;
import com.lovewall.app.api.ApiClient;
import com.lovewall.app.model.Post;
import com.lovewall.app.utils.Prefs;
import com.lovewall.app.utils.ToastUtil;
import java.util.ArrayList;
import java.util.List;

// 管理员端 - 帖子管理
public class AdminPostActivity extends AppCompatActivity {
    private Prefs prefs;
    private RecyclerView rv;
    private AdminPostAdapter adapter;
    private List<Post> posts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_posts);
        prefs = new Prefs(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        rv = findViewById(R.id.recyclerAdminPosts);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminPostAdapter(posts, new AdminPostAdapter.OnActionListener() {
            @Override
            public void onApprove(String id) { updateStatus(id, "published"); }
            @Override
            public void onHide(String id) { updateStatus(id, "hidden"); }
            @Override
            public void onDelete(String id) { deletePost(id); }
        });
        rv.setAdapter(adapter);
        loadPosts();
    }

    private void loadPosts() {
        new Thread(() -> {
            try {
                String resp = ApiClient.get("/api/admin/posts?limit=50", prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                JsonArray arr = json.getAsJsonArray("posts");
                Gson gson = new Gson();
                posts.clear();
                for (JsonElement el : arr) posts.add(gson.fromJson(el, Post.class));
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "加载失败"));
            }
        }).start();
    }

    private void updateStatus(String id, String status) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("status", status);
                ApiClient.put("/api/admin/posts/" + id + "/status", body.toString(), prefs.getToken());
                runOnUiThread(() -> { ToastUtil.show(this, "已更新"); loadPosts(); });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "操作失败"));
            }
        }).start();
    }

    private void deletePost(String id) {
        new AlertDialog.Builder(this).setTitle("确认删除？").setPositiveButton("删除", (d,w) -> {
            new Thread(() -> {
                try {
                    ApiClient.delete("/api/admin/posts/" + id, prefs.getToken());
                    runOnUiThread(() -> { ToastUtil.show(this, "已删除"); loadPosts(); });
                } catch (Exception e) { runOnUiThread(() -> ToastUtil.show(this, "删除失败")); }
            }).start();
        }).setNegativeButton("取消", null).show();
    }
}
