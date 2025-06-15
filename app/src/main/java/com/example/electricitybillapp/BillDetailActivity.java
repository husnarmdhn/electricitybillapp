package com.example.electricitybillapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

public class BillDetailActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private long billId;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize buttons
        Button btnUpdate = findViewById(R.id.btnUpdate);
        Button btnDelete = findViewById(R.id.btnDelete);

        // Initialize and set up back and menu buttons
        ShapeableImageView backButton = findViewById(R.id.back_button);
        ShapeableImageView menuButton = findViewById(R.id.menu_button);

        backButton.setOnClickListener(v -> finish());
        menuButton.setOnClickListener(this::showPopupMenu);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bill Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DBHelper(this);
        billId = getIntent().getLongExtra("BILL_ID", -1);

        if (billId == -1) {
            Toast.makeText(this, "Error: Bill not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayBillDetails(billId);

        // Set up button click listeners
        btnUpdate.setOnClickListener(v -> updateBill());
        btnDelete.setOnClickListener(v -> deleteBill());
    }

    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_bar, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (id == R.id.menu_about) {
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            } else if (id == R.id.menu_logout) {
                signOut();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void signOut() {
        auth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void displayBillDetails(long billId) {
        try (Cursor cursor = dbHelper.getBill(billId)) {
            if (cursor == null || !cursor.moveToFirst()) {
                Toast.makeText(this, "Error loading bill details", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            TextView monthDetail = findViewById(R.id.monthDetail);
            TextView unitsDetail = findViewById(R.id.unitsDetail);
            TextView totalDetail = findViewById(R.id.totalDetail);
            TextView rebateDetail = findViewById(R.id.rebateDetail);
            TextView finalDetail = findViewById(R.id.finalDetail);

            String month = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MONTH));
            int units = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_UNITS));
            double total = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TOTAL));
            double rebate = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_REBATE));
            double finalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_FINAL));

            // Just set the values directly without combining with labels
            monthDetail.setText(month);
            unitsDetail.setText(String.valueOf(units));
            totalDetail.setText(String.format("RM %.2f", total));
            rebateDetail.setText(String.format("%.0f%%", rebate));
            finalDetail.setText(String.format("RM %.2f", finalAmount));

        } catch (Exception e) {
            Toast.makeText(this, "Error displaying bill details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateBill() {
        // Start UpdateBillActivity with the current bill ID
        Intent intent = new Intent(this, UpdateBillActivity.class);
        intent.putExtra("BILL_ID", billId);
        startActivity(intent);
        // Removed finish() here to allow returning to this activity after update
    }

    private void deleteBill() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Bill")
                .setMessage("Are you sure you want to delete this bill?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int deletedRows = dbHelper.deleteRecord(billId);
                    if (deletedRows > 0) {
                        Toast.makeText(this, "Bill deleted successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Return to previous activity
                    } else {
                        Toast.makeText(this, "Failed to delete bill", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the data when returning from UpdateBillActivity
        displayBillDetails(billId);
    }
}