package com.lovewall.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lovewall.app.R;
import com.lovewall.app.api.ApiClient;
import com.lovewall.app.utils.Prefs;
import com.lovewall.app.utils.ToastUtil;

public class AdminDashboardActivity extends AppCompatActivity {
    private Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        prefs = new Prefs(this);

        boolean isSuper = "super_admin".equals(prefs.getRole());
        ((TextView) findViewById(R.id.tvAdminTitle)).setText(isSuper ? "👑 超管后台" : "🔧 管理后台");
        ((TextView) findViewById(R.id.tvAdminName)).setText("欢迎，" + prefs.getNickname());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 功能按钮
        findViewById(R.id.btnManagePosts).setOnClickListener(v -> startActivity(new Intent(this, AdminPostActivity.class)));
        findViewById(R.id.btnManageComments).setOnClickListener(v -> startActivity(new Intent(this, AdminCommentActivity.class)));
        findViewById(R.id.btnManageReports).setOnClickListener(v -> startActivity(new Intent(this, AdminReportActivity.class)));

        // 超管专属
        View superSection = findViewById(R.id.superAdminSection);
        if (isSuper) {
            superSection.setVisibility(android.view.View.VISIBLE);
            findViewById(R.id.btnManageUsers).setOnClickListener(v -> startActivity(new Intent(this, AdminUserActivity.class)));
            findViewById(R.id.btnManageAdmins).setOnClickListener(v -> startActivity(new Intent(this, AdminManageAdminsActivity.class)));
        } else {
            superSection.setVisibility(android.view.View.GONE);
        }

        loadStats();
    }

    private void loadStats() {
        new Thread(() -> {
            try {
                String resp = ApiClient.get("/api/admin/stats", prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                JsonObject s = json.getAsJsonObject("stats");
                runOnUiThread(() -> {
                    ((TextView) findViewById(R.id.statPosts)).setText(String.valueOf(s.get("totalPosts").getAsInt()));
                    ((TextView) findViewById(R.id.statComments)).setText(String.valueOf(s.get("totalComments").getAsInt()));
                    ((TextView) findViewById(R.id.statPending)).setText(String.valueOf(s.get("pendingReview").getAsInt()));
                });
            } catch (Exception e) {}
        }).start();
    }
}
