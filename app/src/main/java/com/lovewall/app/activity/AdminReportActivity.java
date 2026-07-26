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

public class AdminReportActivity extends AppCompatActivity {
    private Prefs prefs;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);
        prefs = new Prefs(this);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        container = findViewById(R.id.reportsContainer);
        loadReports();
    }

    private void loadReports() {
        new Thread(() -> {
            try {
                String resp = ApiClient.get("/api/admin/reports", prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                JsonArray arr = json.getAsJsonArray("reports");
                runOnUiThread(() -> {
                    container.removeAllViews();
                    if (arr.size() == 0) {
                        addText("暂无举报");
                        return;
                    }
                    for (JsonElement el : arr) {
                        JsonObject r = el.getAsJsonObject();
                        String id = r.get("id").getAsString();
                        String reason = r.has("reason") ? r.get("reason").getAsString() : "";
                        String reporter = r.has("reporter_name") ? r.get("reporter_name").getAsString() : "";

                        View item = getLayoutInflater().inflate(R.layout.item_admin_report, container, false);
                        ((TextView) item.findViewById(R.id.tvReporter)).setText("举报人: " + reporter);
                        ((TextView) item.findViewById(R.id.tvReason)).setText("原因: " + reason);
                        item.findViewById(R.id.btnResolve).setOnClickListener(v -> resolve(id, "resolved"));
                        item.findViewById(R.id.btnDismiss).setOnClickListener(v -> resolve(id, "dismissed"));
                        container.addView(item);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "加载失败"));
            }
        }).start();
    }

    private void resolve(String id, String status) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("status", status);
                ApiClient.put("/api/admin/reports/" + id, body.toString(), prefs.getToken());
                runOnUiThread(() -> { ToastUtil.show(this, "已处理"); loadReports(); });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "处理失败"));
            }
        }).start();
    }

    private void addText(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(getResources().getColor(R.color.gray));
        tv.setPadding(32, 32, 32, 32);
        container.addView(tv);
    }
}
