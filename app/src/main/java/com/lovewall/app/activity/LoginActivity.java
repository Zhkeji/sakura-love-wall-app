package com.lovewall.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lovewall.app.R;
import com.lovewall.app.api.ApiClient;
import com.lovewall.app.utils.Prefs;
import com.lovewall.app.utils.ToastUtil;

public class LoginActivity extends AppCompatActivity {
    private LinearLayout loginForm, registerForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginForm = findViewById(R.id.loginForm);
        registerForm = findViewById(R.id.registerForm);

        // 登录按钮
        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String username = ((EditText) findViewById(R.id.etUsername)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.etPassword)).getText().toString().trim();
            if (username.isEmpty() || password.isEmpty()) {
                ToastUtil.show(this, "请输入用户名和密码");
                return;
            }
            doLogin(username, password);
        });

        // 注册按钮
        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String username = ((EditText) findViewById(R.id.etRegUsername)).getText().toString().trim();
            String nickname = ((EditText) findViewById(R.id.etRegNickname)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.etRegPassword)).getText().toString().trim();
            String confirm = ((EditText) findViewById(R.id.etRegConfirm)).getText().toString().trim();
            if (username.isEmpty() || nickname.isEmpty() || password.isEmpty()) {
                ToastUtil.show(this, "请填写所有信息");
                return;
            }
            if (!password.equals(confirm)) {
                ToastUtil.show(this, "两次密码不一致");
                return;
            }
            if (password.length() < 6) {
                ToastUtil.show(this, "密码至少6位");
                return;
            }
            doRegister(username, nickname, password);
        });

        // 切换到注册
        findViewById(R.id.tvSwitchToRegister).setOnClickListener(v -> {
            loginForm.setVisibility(View.GONE);
            registerForm.setVisibility(View.VISIBLE);
            slideIn(registerForm);
        });

        // 切换到登录
        findViewById(R.id.tvSwitchToLogin).setOnClickListener(v -> {
            registerForm.setVisibility(View.GONE);
            loginForm.setVisibility(View.VISIBLE);
            slideIn(loginForm);
        });
    }

    private void doLogin(String username, String password) {
        Button btn = findViewById(R.id.btnLogin);
        btn.setEnabled(false);
        btn.setText("登录中...");

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
                        ToastUtil.show(this, "登录成功 🌸");
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        ToastUtil.show(this, json.has("error") ? json.get("error").getAsString() : "登录失败");
                        btn.setEnabled(true);
                        btn.setText("登 录");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    ToastUtil.show(this, "网络错误");
                    btn.setEnabled(true);
                    btn.setText("登 录");
                });
            }
        }).start();
    }

    private void doRegister(String username, String nickname, String password) {
        Button btn = findViewById(R.id.btnRegister);
        btn.setEnabled(false);
        btn.setText("注册中...");

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
                        ToastUtil.show(this, "注册成功 🌸");
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        ToastUtil.show(this, json.has("error") ? json.get("error").getAsString() : "注册失败");
                        btn.setEnabled(true);
                        btn.setText("注 册");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    ToastUtil.show(this, "网络错误");
                    btn.setEnabled(true);
                    btn.setText("注 册");
                });
            }
        }).start();
    }

    private void slideIn(View view) {
        TranslateAnimation anim = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, -0.5f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f
        );
        anim.setDuration(300);
        AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
        alpha.setDuration(300);
        view.startAnimation(anim);
        view.startAnimation(alpha);
    }
}
