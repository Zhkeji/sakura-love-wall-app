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

public class AdminManageAdminsActivity extends AppCompatActivity {
    private Prefs prefs;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_admins);
        prefs = new Prefs(this);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        container = findViewById(R.id.adminsContainer);

        // 添加管理员按钮
        findViewById(R.id.btnAddAdmin).setOnClickListener(v -> showAddDialog());
        loadAdmins();
    }

    private void loadAdmins() {
        new Thread(() -> {
            try {
                String resp = ApiClient.get("/api/admin/users?role=admin&limit=50", prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                JsonArray arr = json.getAsJsonArray("users");
                runOnUiThread(() -> {
                    container.removeAllViews();
                    if (arr.size() == 0) {
                        TextView tv = new TextView(this);
                        tv.setText("暂无管理员");
                        tv.setTextColor(getResources().getColor(R.color.gray));
                        tv.setPadding(32, 32, 32, 32);
                        container.addView(tv);
                        return;
                    }
                    for (JsonElement el : arr) {
                        JsonObject u = el.getAsJsonObject();
                        String id = u.get("id").getAsString();
                        String username = u.get("username").getAsString();
                        String nickname = u.get("nickname").getAsString();

                        View item = getLayoutInflater().inflate(R.layout.item_admin_admin, container, false);
                        ((TextView) item.findViewById(R.id.tvUsername)).setText(username);
                        ((TextView) item.findViewById(R.id.tvNickname)).setText(nickname);
                        item.findViewById(R.id.btnRemove).setOnClickListener(v -> removeAdmin(id));
                        container.addView(item);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "加载失败"));
            }
        }).start();
    }

    private void showAddDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 0);

        EditText etUser = new EditText(this);
        etUser.setHint("用户名");
        layout.addView(etUser);

        EditText etPass = new EditText(this);
        etPass.setHint("密码");
        etPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPass);

        EditText etNick = new EditText(this);
        etNick.setHint("昵称");
        layout.addView(etNick);

        new android.app.AlertDialog.Builder(this)
            .setTitle("添加管理员")
            .setView(layout)
            .setPositiveButton("创建", (d, w) -> {
                String username = etUser.getText().toString().trim();
                String password = etPass.getText().toString().trim();
                String nickname = etNick.getText().toString().trim();
                if (username.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                    ToastUtil.show(this, "请填写完整");
                    return;
                }
                addAdmin(username, password, nickname);
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void addAdmin(String username, String password, String nickname) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("username", username);
                body.addProperty("password", password);
                body.addProperty("nickname", nickname);
                String resp = ApiClient.post("/api/admin/users/admin", body.toString(), prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                runOnUiThread(() -> {
                    if (json.has("message")) {
                        ToastUtil.show(this, "已添加");
                        loadAdmins();
                    } else {
                        ToastUtil.show(this, json.has("error") ? json.get("error").getAsString() : "添加失败");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "添加失败"));
            }
        }).start();
    }

    private void removeAdmin(String id) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("确认移除？")
            .setPositiveButton("移除", (d, w) -> {
                new Thread(() -> {
                    try {
                        JsonObject body = new JsonObject();
                        body.addProperty("role", "user");
                        ApiClient.put("/api/admin/users/" + id + "/role", body.toString(), prefs.getToken());
                        runOnUiThread(() -> { ToastUtil.show(this, "已移除"); loadAdmins(); });
                    } catch (Exception e) {
                        runOnUiThread(() -> ToastUtil.show(this, "移除失败"));
                    }
                }).start();
            })
            .setNegativeButton("取消", null)
            .show();
    }
}
