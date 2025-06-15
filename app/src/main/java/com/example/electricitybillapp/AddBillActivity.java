package com.example.electricitybillapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import java.text.DecimalFormat;

public class AddBillActivity extends AppCompatActivity {

    private AutoCompleteTextView monthEditText;
    private TextInputEditText unitsEditText;
    private RadioGroup rebateRadioGroup;
    private int selectedRebate = 0;
    private MaterialButton calculateButton, saveButton;
    private DBHelper dbHelper;
    private double totalRM = 0;
    private double finalAmount = 0;
    private FirebaseAuth auth;

    private final String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize views
        monthEditText = findViewById(R.id.monthEditText);
        unitsEditText = findViewById(R.id.unitsEditText);
        rebateRadioGroup = findViewById(R.id.rebateRadioGroup);
        calculateButton = findViewById(R.id.calculateButton);
        saveButton = findViewById(R.id.saveButton);

        // Initialize and set up back and menu buttons
        ShapeableImageView backButton = findViewById(R.id.back_button);
        ShapeableImageView menuButton = findViewById(R.id.menu_button);

        backButton.setOnClickListener(v -> finish());
        menuButton.setOnClickListener(this::showPopupMenu);

        // Set up month dropdown
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, months);
        monthEditText.setAdapter(monthAdapter);

        monthEditText.setOnClickListener(v -> monthEditText.showDropDown());

        // Initialize database helper
        dbHelper = new DBHelper(this);

        // Set up radio group listener
        rebateRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rebate0) selectedRebate = 0;
            else if (checkedId == R.id.rebate1) selectedRebate = 1;
            else if (checkedId == R.id.rebate2) selectedRebate = 2;
            else if (checkedId == R.id.rebate3) selectedRebate = 3;
            else if (checkedId == R.id.rebate4) selectedRebate = 4;
            else if (checkedId == R.id.rebate5) selectedRebate = 5;
        });

        calculateButton.setOnClickListener(v -> calculateBill());
        saveButton.setOnClickListener(v -> saveBill());
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

    private void calculateBill() {
        try {
            String month = monthEditText.getText().toString().trim();
            String unitsStr = unitsEditText.getText().toString().trim();

            if (month.isEmpty()) {
                monthEditText.setError("Please select a month");
                return;
            }

            if (unitsStr.isEmpty()) {
                unitsEditText.setError("Please enter units consumed");
                return;
            }

            int units = Integer.parseInt(unitsStr);
            if (units <= 0) {
                unitsEditText.setError("Units must be greater than 0");
                return;
            }

            double rebate = selectedRebate;
            double totalCharges = calculateBlockRate(units);
            totalRM = totalCharges / 100;
            finalAmount = totalRM * (1 - (rebate / 100));

            DecimalFormat df = new DecimalFormat("0.00");
            showCalculationResults(df.format(totalRM), df.format(finalAmount));

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCalculationResults(String total, String finalCost) {
        new AlertDialog.Builder(this)
                .setTitle("Bill Calculation Results")
                .setMessage("Total Charges: RM" + total + "\nFinal Cost After Rebate: RM" + finalCost)
                .setPositiveButton("OK", null)
                .show();
    }

    private double calculateBlockRate(int units) {
        double total = 0;

        if (units > 200) {
            total += 200 * 21.8;
            units -= 200;
        } else {
            return units * 21.8;
        }

        if (units > 100) {
            total += 100 * 33.4;
            units -= 100;
        } else {
            return total + (units * 33.4);
        }

        if (units > 300) {
            total += 300 * 51.6;
            units -= 300;
        } else {
            return total + (units * 51.6);
        }

        return total + (units * 54.6);
    }

    private void saveBill() {
        String month = monthEditText.getText().toString().trim();
        String unitsStr = unitsEditText.getText().toString().trim();

        if (month.isEmpty()) {
            monthEditText.setError("Month is required");
            return;
        }

        if (unitsStr.isEmpty()) {
            unitsEditText.setError("Units consumed is required");
            return;
        }

        if (totalRM == 0 || finalAmount == 0) {
            Toast.makeText(this, "Please calculate the bill first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int units = Integer.parseInt(unitsStr);
            double rebate = selectedRebate;

            long result = dbHelper.insertRecord(month, units, rebate, totalRM, finalAmount);

            if (result != -1) {
                Toast.makeText(this, "Bill saved successfully", Toast.LENGTH_SHORT).show();
                clearForm();
            } else {
                Toast.makeText(this, "Failed to save bill", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        monthEditText.setText("");
        unitsEditText.setText("");
        rebateRadioGroup.clearCheck();
        selectedRebate = 0;
        totalRM = 0;
        finalAmount = 0;
        monthEditText.requestFocus();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}