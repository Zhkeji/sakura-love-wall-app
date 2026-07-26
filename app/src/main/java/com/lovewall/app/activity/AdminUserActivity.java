package com.lovewall.app.activity;

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
                    for (JsonElement el : arr) {
                        JsonObject u = el.getAsJsonObject();
                        String id = u.get("id").getAsString();
                        String username = u.get("username").getAsString();
                        String nickname = u.get("nickname").getAsString();
                        String role = u.get("role").getAsString();
                        String status = u.get("status").getAsString();

                        View item = getLayoutInflater().inflate(R.layout.item_admin_user, container, false);
                        ((TextView) item.findViewById(R.id.tvUsername)).setText(username);
                        ((TextView) item.findViewById(R.id.tvNickname)).setText(nickname);
                        ((TextView) item.findViewById(R.id.tvRole)).setText(role.equals("super_admin") ? "👑超管" : role.equals("admin") ? "🔧管理" : "👤用户");
                        ((TextView) item.findViewById(R.id.tvStatus)).setText(status.equals("active") ? "✅正常" : "❌封禁");

                        // 只有非自己、非超管才能操作
                        if (!id.equals(prefs.getUserId()) && !"super_admin".equals(role)) {
                            Button btnBan = item.findViewById(R.id.btnBan);
                            btnBan.setVisibility(View.VISIBLE);
                            btnBan.setText(status.equals("active") ? "封禁" : "解封");
                            btnBan.setOnClickListener(v -> toggleStatus(id, status.equals("active") ? "banned" : "active"));
                        }
                        container.addView(item);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "加载失败"));
            }
        }).start();
    }

    private void toggleStatus(String id, String status) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("status", status);
                ApiClient.put("/api/admin/users/" + id + "/status", body.toString(), prefs.getToken());
                runOnUiThread(() -> { ToastUtil.show(this, status.equals("banned") ? "已封禁" : "已解封"); loadUsers(); });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "操作失败"));
            }
        }).start();
    }
}
