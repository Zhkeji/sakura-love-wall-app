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

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etUser = findViewById(R.id.etUsername);
        EditText etNick = findViewById(R.id.etNickname);
        EditText etPass = findViewById(R.id.etPassword);
        EditText etConf = findViewById(R.id.etConfirm);
        Button btnReg = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnReg.setOnClickListener(v -> {
            String username = etUser.getText().toString().trim();
            String nickname = etNick.getText().toString().trim();
            String password = etPass.getText().toString().trim();
            String confirm = etConf.getText().toString().trim();
            if (username.isEmpty() || nickname.isEmpty() || password.isEmpty()) {
                ToastUtil.show(this, "请填写所有信息"); return;
            }
            if (!password.equals(confirm)) {
                ToastUtil.show(this, "两次密码不一致"); return;
            }
            if (password.length() < 6) {
                ToastUtil.show(this, "密码至少6位"); return;
            }
            btnReg.setEnabled(false);

            new Thread(() -> {
                try {
                    JsonObject body = new JsonObject();
                    body.addProperty("username", username);
                    body.addProperty("password", password);
                    body.addProperty("nickname", nickname);
                    String resp = ApiClient.post("/api/auth/register", body.toString(), null);
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
                            ToastUtil.show(this, "注册成功");
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            ToastUtil.show(this, json.has("error") ? json.get("error").getAsString() : "注册失败");
                            btnReg.setEnabled(true);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        ToastUtil.show(this, "网络错误");
                        btnReg.setEnabled(true);
                    });
                }
            }).start();
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
