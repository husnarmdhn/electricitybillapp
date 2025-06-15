package com.example.electricitybillapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    // Database constants
    private static final String DATABASE_NAME = "bill.db";
    private static final int DATABASE_VERSION = 1;

    // Table and column constants
    public static final String TABLE_RECORDS = "records";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_UNITS = "units";
    public static final String COLUMN_REBATE = "rebate";
    public static final String COLUMN_TOTAL = "total";
    public static final String COLUMN_FINAL = "final_amount";
    public static final String COLUMN_DATE = "date"; // Added date column for sorting

    // SQL to create table
    private static final String CREATE_TABLE_RECORDS =
            "CREATE TABLE " + TABLE_RECORDS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MONTH + " TEXT NOT NULL, " +
                    COLUMN_UNITS + " INTEGER NOT NULL, " +
                    COLUMN_REBATE + " REAL DEFAULT 0, " +
                    COLUMN_TOTAL + " REAL NOT NULL, " +
                    COLUMN_FINAL + " REAL NOT NULL, " +
                    COLUMN_DATE + " TEXT DEFAULT CURRENT_TIMESTAMP)"; // Added date column

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_RECORDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDS);
        onCreate(db);
    }

    // Insert a new bill record
    public long insertRecord(String month, int units, double rebate, double total, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_UNITS, units);
        values.put(COLUMN_REBATE, rebate);
        values.put(COLUMN_TOTAL, total);
        values.put(COLUMN_FINAL, finalCost);
        // Date will be automatically set to current timestamp

        return db.insert(TABLE_RECORDS, null, values);
    }

    // Get all bills sorted by month
    public Cursor getAllBills() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RECORDS,
                new String[]{COLUMN_ID, COLUMN_MONTH, COLUMN_UNITS, COLUMN_REBATE, COLUMN_TOTAL, COLUMN_FINAL},
                null, null, null, null,
                COLUMN_MONTH + " ASC");
    }

    // Get bills grouped by month
    public Cursor getBillsGroupedByMonth() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + COLUMN_MONTH + ", SUM(" + COLUMN_FINAL + ") as total_amount " +
                "FROM " + TABLE_RECORDS + " " +
                "GROUP BY " + COLUMN_MONTH + " " +
                "ORDER BY " + COLUMN_DATE + " DESC", null);
    }

    // Get all bills for a specific month
    public Cursor getBillsForMonth(String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RECORDS,
                new String[]{COLUMN_ID, COLUMN_MONTH, COLUMN_UNITS, COLUMN_REBATE, COLUMN_FINAL, COLUMN_DATE},
                COLUMN_MONTH + " = ?",
                new String[]{month},
                null, null,
                COLUMN_DATE + " DESC");
    }

    // Get a specific bill by ID
    public Cursor getBill(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RECORDS,
                null, // Get all columns
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);
    }

    // Update an existing bill record
    public int updateRecord(long id, String month, int units, double rebate, double total, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_UNITS, units);
        values.put(COLUMN_REBATE, rebate);
        values.put(COLUMN_TOTAL, total);
        values.put(COLUMN_FINAL, finalCost);

        return db.update(TABLE_RECORDS, values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // Delete a bill record
    public int deleteRecord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_RECORDS,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // Get total units consumed (for statistics)
    public int getTotalUnits() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_UNITS + ") FROM " + TABLE_RECORDS, null);
        if (cursor.moveToFirst()) {
            return cursor.getInt(0);
        }
        return 0;
    }

    // Get total amount spent (for statistics)
    public double getTotalAmount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_FINAL + ") FROM " + TABLE_RECORDS, null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        return 0.0;
    }

    // Close the database when done
    @Override
    public void close() {
        super.close();
    }
}