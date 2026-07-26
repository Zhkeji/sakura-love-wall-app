package com.lovewall.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lovewall.app.R;
import com.lovewall.app.api.ApiClient;
import com.lovewall.app.utils.Prefs;
import com.lovewall.app.utils.ToastUtil;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Prefs prefs = new Prefs(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 个人信息卡片
        ImageView imgAvatar = findViewById(R.id.imgAvatar);
        TextView tvNickname = findViewById(R.id.tvNickname);
        TextView tvBio = findViewById(R.id.tvBio);
        EditText etNick = findViewById(R.id.etNickname);
        EditText etBio = findViewById(R.id.etBio);

        tvNickname.setText(prefs.getNickname());
        etNick.setText(prefs.getNickname());

        // 管理员入口
        String role = prefs.getRole();
        if ("admin".equals(role) || "super_admin".equals(role)) {
            Button btnAdmin = findViewById(R.id.btnAdmin);
            btnAdmin.setVisibility(View.VISIBLE);
            btnAdmin.setText("admin".equals(role) ? "🔧 管理员后台" : "👑 超管后台");
            btnAdmin.setOnClickListener(v -> startActivity(new Intent(this, AdminDashboardActivity.class)));
        }

        // 加载个人信息
        new Thread(() -> {
            try {
                String resp = ApiClient.get("/api/auth/me", prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                JsonObject user = json.getAsJsonObject("user");
                runOnUiThread(() -> {
                    tvNickname.setText(user.get("nickname").getAsString());
                    etNick.setText(user.get("nickname").getAsString());
                    if (user.has("bio") && !user.get("bio").isJsonNull()) {
                        tvBio.setText(user.get("bio").getAsString());
                        etBio.setText(user.get("bio").getAsString());
                    }
                });
            } catch (Exception e) {}
        }).start();

        // 保存资料
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            String nickname = etNick.getText().toString().trim();
            String bio = etBio.getText().toString().trim();
            if (nickname.isEmpty()) { ToastUtil.show(this, "昵称不能为空"); return; }

            new Thread(() -> {
                try {
                    JsonObject body = new JsonObject();
                    body.addProperty("nickname", nickname);
                    body.addProperty("bio", bio);
                    String resp = ApiClient.put("/api/auth/profile", body.toString(), prefs.getToken());
                    JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                    runOnUiThread(() -> {
                        if (json.has("user")) {
                            JsonObject user = json.getAsJsonObject("user");
                            prefs.setNickname(user.get("nickname").getAsString());
                            tvNickname.setText(user.get("nickname").getAsString());
                            tvBio.setText(bio);
                            ToastUtil.show(this, "保存成功");
                        } else {
                            ToastUtil.show(this, json.has("error") ? json.get("error").getAsString() : "保存失败");
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> ToastUtil.show(this, "网络错误"));
                }
            }).start();
        });

        // 修改密码
        findViewById(R.id.btnChangePwd).setOnClickListener(v -> {
            String oldPwd = ((EditText) findViewById(R.id.etOldPwd)).getText().toString().trim();
            String newPwd = ((EditText) findViewById(R.id.etNewPwd)).getText().toString().trim();
            if (oldPwd.isEmpty() || newPwd.isEmpty()) { ToastUtil.show(this, "请输入密码"); return; }
            if (newPwd.length() < 6) { ToastUtil.show(this, "新密码至少6位"); return; }

            new Thread(() -> {
                try {
                    JsonObject body = new JsonObject();
                    body.addProperty("oldPassword", oldPwd);
                    body.addProperty("newPassword", newPwd);
                    String resp = ApiClient.put("/api/auth/password", body.toString(), prefs.getToken());
                    JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                    runOnUiThread(() -> {
                        ToastUtil.show(this, json.has("message") ? json.get("message").getAsString() : json.has("error") ? json.get("error").getAsString() : "操作完成");
                        if (json.has("message")) {
                            ((EditText) findViewById(R.id.etOldPwd)).setText("");
                            ((EditText) findViewById(R.id.etNewPwd)).setText("");
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> ToastUtil.show(this, "网络错误"));
                }
            }).start();
        });

        // 关于
        findViewById(R.id.btnAbout).setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));

        // 退出登录
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            prefs.clear();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }
}
