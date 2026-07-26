package com.lovewall.app.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lovewall.app.R;
import com.lovewall.app.api.ApiClient;
import com.lovewall.app.utils.Prefs;
import com.lovewall.app.utils.ToastUtil;

public class CreatePostActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        Prefs prefs = new Prefs(this);

        EditText etTitle = findViewById(R.id.etTitle);
        EditText etContent = findViewById(R.id.etContent);
        EditText etTags = findViewById(R.id.etTags);
        CheckBox cbAnon = findViewById(R.id.cbAnonymous);
        Button btnPublish = findViewById(R.id.btnPublish);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnPublish.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            String tagsStr = etTags.getText().toString().trim();
            boolean anon = cbAnon.isChecked();

            if (title.isEmpty() || content.isEmpty()) {
                ToastUtil.show(this, "请输入标题和内容");
                return;
            }

            btnPublish.setEnabled(false);
            btnPublish.setText("发布中...");

            new Thread(() -> {
                try {
                    JsonObject body = new JsonObject();
                    body.addProperty("title", title);
                    body.addProperty("content", content);
                    body.addProperty("isAnonymous", anon);
                    JsonArray tagsArr = new JsonArray();
                    if (!tagsStr.isEmpty()) {
                        for (String t : tagsStr.split("[,，]")) {
                            String tag = t.trim();
                            if (!tag.isEmpty()) tagsArr.add(tag);
                        }
                    }
                    body.add("tags", tagsArr);
                    body.add("images", new JsonArray());

                    String resp = ApiClient.post("/api/posts", body.toString(), prefs.getToken());
                    JsonObject json = JsonParser.parseString(resp).getAsJsonObject();

                    runOnUiThread(() -> {
                        if (json.has("message")) {
                            ToastUtil.show(this, json.get("message").getAsString());
                            finish();
                        } else {
                            ToastUtil.show(this, json.has("error") ? json.get("error").getAsString() : "发布失败");
                            btnPublish.setEnabled(true);
                            btnPublish.setText("发布表白");
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        ToastUtil.show(this, "网络错误");
                        btnPublish.setEnabled(true);
                        btnPublish.setText("发布表白");
                    });
                }
            }).start();
        });
    }
}
