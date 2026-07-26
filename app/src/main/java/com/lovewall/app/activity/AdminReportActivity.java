package com.lovewall.app.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.lovewall.app.R;

public class AdminReportActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
