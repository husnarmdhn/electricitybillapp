package com.example.electricitybillapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import java.text.DecimalFormat;

public class UpdateBillActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private long billId;
    private AutoCompleteTextView etMonth;
    private TextInputEditText etUnits;
    private RadioGroup rebateRadioGroup;
    private MaterialButton btnUpdate, btnCalculate;
    private double totalRM = 0;
    private double finalAmount = 0;

    private final String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_bill);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize back and menu buttons
        ShapeableImageView backButton = findViewById(R.id.back_button);
        ShapeableImageView menuButton = findViewById(R.id.menu_button);

        backButton.setOnClickListener(v -> finish());
        menuButton.setOnClickListener(this::showPopupMenu);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Update Bill");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get bill ID and validate
        billId = getIntent().getLongExtra("BILL_ID", -1);
        if (billId == -1) {
            Toast.makeText(this, "Invalid bill ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DBHelper(this);

        // Initialize views
        etMonth = findViewById(R.id.monthEditText);
        etUnits = findViewById(R.id.unitsEditText);
        rebateRadioGroup = findViewById(R.id.rebateRadioGroup);
        btnUpdate = findViewById(R.id.updateButton);
        btnCalculate = findViewById(R.id.calculateButton);

        // Set up month dropdown
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, months);
        etMonth.setAdapter(monthAdapter);
        etMonth.setOnClickListener(v -> etMonth.showDropDown());

        // Set click listeners
        btnUpdate.setOnClickListener(v -> updateBill());
        btnCalculate.setOnClickListener(v -> calculateBill());

        loadBillData();
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

    private void loadBillData() {
        try (Cursor cursor = dbHelper.getBill(billId)) {
            if (cursor != null && cursor.moveToFirst()) {
                etMonth.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MONTH)));
                etUnits.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_UNITS))));

                double rebate = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_REBATE));
                int rebatePercent = (int) rebate;
                switch (rebatePercent) {
                    case 0: rebateRadioGroup.check(R.id.rebate0); break;
                    case 1: rebateRadioGroup.check(R.id.rebate1); break;
                    case 2: rebateRadioGroup.check(R.id.rebate2); break;
                    case 3: rebateRadioGroup.check(R.id.rebate3); break;
                    case 4: rebateRadioGroup.check(R.id.rebate4); break;
                    case 5: rebateRadioGroup.check(R.id.rebate5); break;
                }

                // Load previous calculated values
                totalRM = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TOTAL));
                finalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_FINAL));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading bill data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void calculateBill() {
        try {
            String month = etMonth.getText().toString().trim();
            String unitsStr = etUnits.getText().toString().trim();

            if (month.isEmpty()) {
                etMonth.setError("Please select a month");
                return;
            }

            if (unitsStr.isEmpty()) {
                etUnits.setError("Please enter units consumed");
                return;
            }

            int units = Integer.parseInt(unitsStr);
            if (units <= 0) {
                etUnits.setError("Units must be greater than 0");
                return;
            }

            int selectedRebateId = rebateRadioGroup.getCheckedRadioButtonId();
            if (selectedRebateId == -1) {
                Toast.makeText(this, "Please select a rebate percentage", Toast.LENGTH_SHORT).show();
                return;
            }

            double rebate = getRebatePercentage(selectedRebateId);
            double totalCharges = calculateBlockRate(units); // Calculate in sen
            totalRM = totalCharges / 100; // Convert to RM
            finalAmount = totalRM * (1 - rebate/100);

            showCalculationResults(totalRM, finalAmount, rebate);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateBlockRate(int units) {
        double total = 0;

        if (units > 200) {
            total += 200 * 21.8; // First 200 units at 21.8 sen
            units -= 200;
        } else {
            return units * 21.8;
        }

        if (units > 100) {
            total += 100 * 33.4; // Next 100 units at 33.4 sen
            units -= 100;
        } else {
            return total + (units * 33.4);
        }

        if (units > 300) {
            total += 300 * 51.6; // Next 300 units at 51.6 sen
            units -= 300;
        } else {
            return total + (units * 51.6);
        }

        return total + (units * 54.6); // Remaining units at 54.6 sen
    }

    private void showCalculationResults(double total, double finalCost, double rebate) {
        DecimalFormat df = new DecimalFormat("0.00");
        new AlertDialog.Builder(this)
                .setTitle("Bill Calculation Results")
                .setMessage(String.format(
                        "Total Charges: RM%s\n" +
                                "Final Cost After Rebate: RM%s",
                        df.format(total),
                        rebate,
                        df.format(finalCost)))
                .setPositiveButton("OK", null)
                .show();
    }

    private void updateBill() {
        String month = etMonth.getText().toString().trim();
        String unitsStr = etUnits.getText().toString().trim();

        if (month.isEmpty() || unitsStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedRebateId = rebateRadioGroup.getCheckedRadioButtonId();
        if (selectedRebateId == -1) {
            Toast.makeText(this, "Please select a rebate percentage", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int units = Integer.parseInt(unitsStr);
            double rebate = getRebatePercentage(selectedRebateId);

            if (totalRM == 0 || finalAmount == 0) {
                // Recalculate if not calculated yet
                totalRM = calculateTotal(units);
                finalAmount = totalRM * (1 - rebate/100);
            }

            int updatedRows = dbHelper.updateRecord(billId, month, units, rebate, totalRM, finalAmount);

            if (updatedRows > 0) {
                Toast.makeText(this, "Bill updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update bill", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateTotal(int units) {
        // Electricity rate calculation (in RM)
        if (units <= 200) {
            return units * 0.218; // 21.8 sen per unit for first 200
        } else if (units <= 300) {
            return (200 * 0.218) + ((units - 200) * 0.334); // 33.4 sen for next 100
        } else {
            return (200 * 0.218) + (100 * 0.334) + ((units - 300) * 0.516); // 51.6 sen beyond 300
        }
    }

    private double getRebatePercentage(int selectedRebateId) {
        if (selectedRebateId == R.id.rebate0) return 0;
        else if (selectedRebateId == R.id.rebate1) return 1;
        else if (selectedRebateId == R.id.rebate2) return 2;
        else if (selectedRebateId == R.id.rebate3) return 3;
        else if (selectedRebateId == R.id.rebate4) return 4;
        else if (selectedRebateId == R.id.rebate5) return 5;
        return 0;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    private void signOut() {
        auth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}