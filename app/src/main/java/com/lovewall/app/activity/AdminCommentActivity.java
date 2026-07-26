package com.lovewall.app.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.lovewall.app.R;

public class AdminCommentActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_comments);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        // 评论管理逻辑同帖子管理，简化实现
    }
}
