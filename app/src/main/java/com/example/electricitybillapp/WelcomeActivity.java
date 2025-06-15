package com.example.electricitybillapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button startButton = findViewById(R.id.start_button);
        Button aboutButton = findViewById(R.id.about_button);

        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        aboutButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }
}