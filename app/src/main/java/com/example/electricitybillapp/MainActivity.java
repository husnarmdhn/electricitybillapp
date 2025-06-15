package com.example.electricitybillapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button signupButton = findViewById(R.id.signup_button);
        Button loginButton = findViewById(R.id.login_button);

        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    public void openRegister(View v){
        Intent i = new Intent(this,SignupActivity.class);
        startActivity(i);
    }

    public void openLogin(View v){
        Intent i = new Intent(this,LoginActivity.class);
        startActivity(i);
    }
}