package com.example.electricitybillapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewBillActivity extends AppCompatActivity {

    private static final String TAG = "ViewBillActivity";
    private ExpandableListView expandableListView;
    private DBHelper dbHelper;
    private List<String> listDataHeader;
    private Map<String, List<Bill>> listDataChild;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bill);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize and set up back and menu buttons
        ShapeableImageView backButton = findViewById(R.id.back_button);
        ShapeableImageView menuButton = findViewById(R.id.menu_button);

        backButton.setOnClickListener(v -> finish());
        menuButton.setOnClickListener(this::showPopupMenu);

        expandableListView = findViewById(R.id.expandableListView);
        dbHelper = new DBHelper(this);

        // Initialize data structures
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        // Load data from database
        loadBillData();

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                try {
                    Bill bill = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                    Intent intent = new Intent(ViewBillActivity.this, BillDetailActivity.class);
                    intent.putExtra("BILL_ID", bill.getId());
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Error opening bill details: " + e.getMessage());
                    Toast.makeText(ViewBillActivity.this, "Error opening bill", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });
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

    private void loadBillData() {
        try {
            Cursor cursor = dbHelper.getAllBills();
            List<Bill> bills = new ArrayList<>();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Bill bill = new Bill(
                            cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_MONTH)),
                            cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_UNITS)),
                            cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_REBATE)),
                            cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL)),
                            cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_FINAL))
                    );
                    bills.add(bill);
                } while (cursor.moveToNext());
                cursor.close();
            }

            prepareListData(bills);
            ExpandableListAdapter adapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
            expandableListView.setAdapter(adapter);

        } catch (Exception e) {
            Log.e(TAG, "Error loading bills: " + e.getMessage());
            Toast.makeText(this, "Error loading bills", Toast.LENGTH_SHORT).show();
        }
    }

    private void prepareListData(List<Bill> bills) {
        listDataHeader.clear();
        listDataChild.clear();

        // Define the order of months
        String[] monthOrder = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        // Group bills by month
        Map<String, List<Bill>> billsByMonth = new HashMap<>();
        for (Bill bill : bills) {
            String month = bill.getMonth();

            if (!billsByMonth.containsKey(month)) {
                billsByMonth.put(month, new ArrayList<Bill>());
            }
            billsByMonth.get(month).add(bill);
        }

        // Add months in order if they exist in the data
        for (String month : monthOrder) {
            if (billsByMonth.containsKey(month)) {
                listDataHeader.add(month);
                listDataChild.put(month, billsByMonth.get(month));
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // Inner class for the ExpandableListAdapter
    private class ExpandableListAdapter extends BaseExpandableListAdapter {
        private Context context;
        private List<String> listDataHeader;
        private Map<String, List<Bill>> listDataChild;

        public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                     Map<String, List<Bill>> listDataChild) {
            this.context = context;
            this.listDataHeader = listDataHeader;
            this.listDataChild = listDataChild;
        }

        @Override
        public int getGroupCount() {
            return listDataHeader.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return listDataChild.get(listDataHeader.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return listDataHeader.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
            }

            TextView lblListHeader = convertView.findViewById(android.R.id.text1);
            lblListHeader.setText(headerTitle);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            Bill bill = (Bill) getChild(groupPosition, childPosition);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(android.R.layout.simple_list_item_2, null);
            }

            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);

            text1.setText(bill.getMonth() + " - " + bill.getUnits() + " units");
            text2.setText(String.format("RM %.2f", bill.getFinalAmount()));

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}