package com.example.qldb.ActiVity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qldb.R;

public class SettingActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);

        LinearLayout layoutAccount = findViewById(R.id.layoutAccount);

        layoutAccount.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, AccountActivity.class);
            startActivity(intent);
        });

    }


}