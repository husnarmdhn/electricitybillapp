package com.example.electricitybillapp; // Replace with your package name

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Make website link clickable
        TextView websiteLink = findViewById(R.id.websiteLink);
        websiteLink.setMovementMethod(LinkMovementMethod.getInstance());
        websiteLink.setOnClickListener(v -> {
            String url = "https://example.com"; // Replace with your actual website URL
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        // Back button functionality
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
}