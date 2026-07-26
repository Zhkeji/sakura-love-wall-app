package com.lovewall.app.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.lovewall.app.R;

public class AdminManageAdminsActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_admins);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
