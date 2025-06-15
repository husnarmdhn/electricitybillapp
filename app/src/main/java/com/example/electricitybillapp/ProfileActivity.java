package com.example.electricitybillapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu; // Use PopupMenu, not ImageButton for menu_button
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    TextView profileText; // Declare TextView

    @SuppressLint("MissingInflatedId") // You can still keep this for other views if needed, but it's good practice to ensure all views are found.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser(); // Get current user

        // Initialize profileText after setContentView
        profileText = findViewById(R.id.profileText); // Correctly find the TextView by its ID

        // Check if user is logged in and update the TextView
        if (user != null) {
            profileText.setText("Welcome, " + user.getEmail() + "!"); // Display user's email
        } else {
            profileText.setText("Welcome, Guest!"); // Or "No user logged in"
            // Optionally, redirect to login if no user is found
            // startActivity(new Intent(this, MainActivity.class));
            // finish();
        }

        // Initialize buttons
        Button addBillButton = findViewById(R.id.add_bill_button);
        Button viewBillButton = findViewById(R.id.view_bill_button);
        Button logoutButton = findViewById(R.id.logout_button);

        // Set up click listeners for main buttons
        addBillButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AddBillActivity.class);
            startActivity(intent);
        });

        viewBillButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ViewBillActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> signout());

        // Initialize ShapeableImageViews for back and menu buttons
        ShapeableImageView backButton = findViewById(R.id.back_button);
        ShapeableImageView menuButton = findViewById(R.id.menu_button);

        // Set up click listeners for back and menu buttons
        backButton.setOnClickListener(v -> finish()); // Go back to the previous activity

        menuButton.setOnClickListener(this::showPopupMenu);
    }

    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        // Ensure you have a 'menu_bar.xml' in your 'res/menu' directory
        popup.getMenuInflater().inflate(R.menu.menu_bar, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                // Already on ProfileActivity, so no need to start it again unless you want to refresh
                // If you want to go to a different home screen, change this.
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (id == R.id.menu_about) {
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            } else if (id == R.id.menu_logout) {
                signout();
                return true;
            }
            return false;
        });
        popup.show();
    }

    public void signout() {
        if (auth != null) {
            auth.signOut();
        }
        // Redirect to MainActivity (Login/Welcome screen) after logout
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(i);
        finish(); // Finish the current activity
    }
}