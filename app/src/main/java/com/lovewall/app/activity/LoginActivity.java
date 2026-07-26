package com.lovewall.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lovewall.app.R;
import com.lovewall.app.api.ApiClient;
import com.lovewall.app.utils.Prefs;
import com.lovewall.app.utils.ToastUtil;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etUser = findViewById(R.id.etUsername);
        EditText etPass = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvReg = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> {
            String username = etUser.getText().toString().trim();
            String password = etPass.getText().toString().trim();
            if (username.isEmpty() || password.isEmpty()) {
                ToastUtil.show(this, "请输入用户名和密码");
                return;
            }
            btnLogin.setEnabled(false);
            btnLogin.setText("登录中...");

            new Thread(() -> {
                try {
                    JsonObject body = new JsonObject();
                    body.addProperty("username", username);
                    body.addProperty("password", password);
                    String resp = ApiClient.post("/api/auth/login", body.toString(), null);
                    JsonObject json = JsonParser.parseString(resp).getAsJsonObject();

                    runOnUiThread(() -> {
                        if (json.has("token")) {
                            JsonObject user = json.getAsJsonObject("user");
                            new Prefs(this).saveLogin(
                                json.get("token").getAsString(),
                                user.get("id").getAsString(),
                                user.get("username").getAsString(),
                                user.get("nickname").getAsString(),
                                user.has("avatar") ? user.get("avatar").getAsString() : "",
                                user.get("role").getAsString()
                            );
                            ToastUtil.show(this, "登录成功");
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            ToastUtil.show(this, json.has("error") ? json.get("error").getAsString() : "登录失败");
                            btnLogin.setEnabled(true);
                            btnLogin.setText("登录");
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        ToastUtil.show(this, "网络错误: " + e.getMessage());
                        btnLogin.setEnabled(true);
                        btnLogin.setText("登录");
                    });
                }
            }).start();
        });

        tvReg.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }
}
