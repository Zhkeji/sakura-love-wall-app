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

public class AdminUserActivity extends AppCompatActivity {
    private Prefs prefs;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);
        prefs = new Prefs(this);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        container = findViewById(R.id.usersContainer);
        loadUsers();
    }

    private void loadUsers() {
        new Thread(() -> {
            try {
                String resp = ApiClient.get("/api/admin/users?limit=50", prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                JsonArray arr = json.getAsJsonArray("users");
                runOnUiThread(() -> {
                    container.removeAllViews();
                    for (com.google.gson.JsonElement el : arr) {
                        JsonObject u = el.getAsJsonObject();
                        String id = u.get("id").getAsString();
                        String username = u.get("username").getAsString();
                        String nickname = u.get("nickname").getAsString();
                        String role = u.get("role").getAsString();
                        String status = u.get("status").getAsString();

                        View item = getLayoutInflater().inflate(R.layout.item_admin_user, container, false);
                        ((TextView) item.findViewById(R.id.tvUsername)).setText(username);
                        ((TextView) item.findViewById(R.id.tvNickname)).setText(nickname);
                        ((TextView) item.findViewById(R.id.tvRole)).setText(
                            role.equals("super_admin") ? "👑超管" : role.equals("admin") ? "🔧管理" : "👤用户"
                        );
                        ((TextView) item.findViewById(R.id.tvStatus)).setText(
                            status.equals("active") ? "✅正常" : "❌封禁"
                        );

                        // 操作按钮
                        Button btnBan = item.findViewById(R.id.btnBan);
                        btnBan.setVisibility(View.VISIBLE);
                        btnBan.setText(status.equals("active") ? "封禁" : "解封");
                        btnBan.setOnClickListener(v -> {
                            if (status.equals("active")) {
                                showBanDialog(id);
                            } else {
                                unbanUser(id);
                            }
                        });

                        // 详情按钮
                        Button btnDetail = item.findViewById(R.id.btnDetail);
                        if (btnDetail != null) {
                            btnDetail.setVisibility(View.VISIBLE);
                            btnDetail.setOnClickListener(v -> showUserDetail(id));
                        }

                        container.addView(item);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "加载失败"));
            }
        }).start();
    }

    private void showBanDialog(String userId) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 0);

        EditText etReason = new EditText(this);
        etReason.setHint("封禁理由（选填）");
        layout.addView(etReason);

        Spinner spinner = new Spinner(this);
        String[] durations = {"永久封禁", "1小时", "1天", "7天", "30天"};
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, durations));
        layout.addView(spinner);

        new AlertDialog.Builder(this)
            .setTitle("🚫 封禁用户")
            .setView(layout)
            .setPositiveButton("确认封禁", (d, w) -> {
                String reason = etReason.getText().toString().trim();
                int[] durationsMin = {0, 60, 1440, 10080, 43200};
                int duration = durationsMin[spinner.getSelectedItemPosition()];
                banUser(userId, reason, duration);
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void banUser(String userId, String reason, int duration) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("reason", reason);
                if (duration > 0) body.addProperty("duration", duration);
                ApiClient.put("/api/admin/users/" + userId + "/ban", body.toString(), prefs.getToken());
                runOnUiThread(() -> { ToastUtil.show(this, "已封禁"); loadUsers(); });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "封禁失败"));
            }
        }).start();
    }

    private void unbanUser(String userId) {
        new Thread(() -> {
            try {
                ApiClient.put("/api/admin/users/" + userId + "/unban", "{}", prefs.getToken());
                runOnUiThread(() -> { ToastUtil.show(this, "已解封"); loadUsers(); });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "解封失败"));
            }
        }).start();
    }

    private void showUserDetail(String userId) {
        new Thread(() -> {
            try {
                String resp = ApiClient.get("/api/admin/users/" + userId + "/detail", prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                JsonObject user = json.getAsJsonObject("user");
                JsonObject stats = json.getAsJsonObject("stats");

                runOnUiThread(() -> {
                    String info = "用户名: " + user.get("username").getAsString() + "\n"
                        + "昵称: " + user.get("nickname").getAsString() + "\n"
                        + "角色: " + user.get("role").getAsString() + "\n"
                        + "状态: " + user.get("status").getAsString() + "\n"
                        + "帖子: " + stats.get("postCount").getAsInt() + "\n"
                        + "评论: " + stats.get("commentCount").getAsInt() + "\n"
                        + "获赞: " + stats.get("likeCount").getAsInt();

                    new AlertDialog.Builder(this)
                        .setTitle("👤 用户详情")
                        .setMessage(info)
                        .setPositiveButton("确定", null)
                        .show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "获取详情失败"));
            }
        }).start();
    }
}
