package com.lovewall.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lovewall.app.R;
import com.lovewall.app.api.ApiClient;
import com.lovewall.app.utils.ToastUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        String version = getAppVersion();
        ((TextView) findViewById(R.id.tvVersion)).setText("版本 " + version);

        Button btnCheck = findViewById(R.id.btnCheckUpdate);
        TextView tvStatus = findViewById(R.id.tvUpdateStatus);

        btnCheck.setOnClickListener(v -> {
            btnCheck.setEnabled(false);
            btnCheck.setText("检查中...");
            tvStatus.setText("");

            new Thread(() -> {
                try {
                    URL url = new URL(ApiClient.UPDATE_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setRequestProperty("User-Agent", "LoveWallApp/Android");

                    int code = conn.getResponseCode();
                    if (code != 200) throw new Exception("HTTP " + code);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();
                    conn.disconnect();

                    JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject();
                    int latestCode = json.get("versionCode").getAsInt();
                    String latestName = json.get("versionName").getAsString();
                    String downloadUrl = json.get("downloadUrl").getAsString();
                    String updateLog = json.has("updateLog") ? json.get("updateLog").getAsString() : "";

                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    int currentCode = pInfo.versionCode;

                    if (latestCode > currentCode) {
                        runOnUiThread(() -> {
                            btnCheck.setEnabled(true);
                            btnCheck.setText("检查更新");
                            new AlertDialog.Builder(this)
                                .setTitle("发现新版本 v" + latestName)
                                .setMessage("更新内容：\n" + updateLog)
                                .setPositiveButton("立即下载", (d, w) -> {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                                    startActivity(intent);
                                })
                                .setNegativeButton("稍后再说", null)
                                .show();
                        });
                    } else {
                        runOnUiThread(() -> {
                            btnCheck.setEnabled(true);
                            btnCheck.setText("检查更新");
                            tvStatus.setText("✅ 已是最新版本");
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        btnCheck.setEnabled(true);
                        btnCheck.setText("检查更新");
                        tvStatus.setText("检查失败: " + e.getMessage());
                    });
                }
            }).start();
        });
    }

    private String getAppVersion() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            return "1.0.0";
        }
    }
}
