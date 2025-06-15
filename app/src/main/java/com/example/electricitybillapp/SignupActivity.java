package com.example.electricitybillapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    EditText e1, e2;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Fixed version using android.R.id.content
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        e1 = findViewById(R.id.editText);
        e2 = findViewById(R.id.editText2);

        mAuth = FirebaseAuth.getInstance();

        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(this::createUser);
    }

    public void createUser(View v) {
        if (e1.getText().toString().equals("") && e2.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Blank not allowed", Toast.LENGTH_SHORT).show();
        } else {
            String email = e1.getText().toString();
            String password = e2.getText().toString();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "User created successfully", Toast.LENGTH_SHORT).show();
                                finish();
                                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                                startActivity(i);
                            } else {
                                Toast.makeText(getApplicationContext(), "User could not be created", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}