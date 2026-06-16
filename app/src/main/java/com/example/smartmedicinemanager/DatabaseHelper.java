package com.example.smartmedicinemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MedicineDB";
    public static final int DATABASE_VERSION = 5;

    // Medicines Table
    public static final String TABLE_NAME = "medicines";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DOSAGE = "dosage";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_PILL_COUNT = "pill_count";
    public static final String COLUMN_EXPIRY_DATE = "expiry_date";
    public static final String COLUMN_USER_EMAIL = "user_email";

    // History Table
    public static final String HISTORY_TABLE = "history";
    public static final String HISTORY_ID = "history_id";
    public static final String HISTORY_MEDICINE_NAME = "medicine_name";
    public static final String HISTORY_STATUS = "status";
    public static final String HISTORY_TIME = "time_taken";
    public static final String HISTORY_USER_EMAIL = "user_email";

    // Users Table
    public static final String USERS_TABLE = "users";
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "name";
    public static final String USER_EMAIL = "email";
    public static final String USER_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createMedicineTable =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME + " TEXT, " +
                        COLUMN_DOSAGE + " TEXT, " +
                        COLUMN_TIME + " TEXT, " +
                        COLUMN_PILL_COUNT + " INTEGER, " +
                        COLUMN_EXPIRY_DATE + " TEXT, " +
                        COLUMN_USER_EMAIL + " TEXT)";

        String createHistoryTable =
                "CREATE TABLE " + HISTORY_TABLE + " (" +
                        HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        HISTORY_MEDICINE_NAME + " TEXT, " +
                        HISTORY_STATUS + " TEXT, " +
                        HISTORY_TIME + " TEXT, " +
                        HISTORY_USER_EMAIL + " TEXT)";

        String createUsersTable =
                "CREATE TABLE " + USERS_TABLE + " (" +
                        USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USER_NAME + " TEXT, " +
                        USER_EMAIL + " TEXT, " +
                        USER_PASSWORD + " TEXT)";

        db.execSQL(createMedicineTable);
        db.execSQL(createHistoryTable);
        db.execSQL(createUsersTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
        onCreate(db);
    }

    public boolean insertUser(String name, String email, String password) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_NAME, name);
        values.put(USER_EMAIL, email);
        values.put(USER_PASSWORD, password);

        long result = db.insert(USERS_TABLE, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + USERS_TABLE + " WHERE email=? AND password=?",
                new String[]{email, password}
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();

        return exists;
    }

    public String getUserName(String email) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT name FROM " + USERS_TABLE + " WHERE email=?",
                new String[]{email}
        );

        String name = "User";

        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }

        cursor.close();
        return name;
    }

    public boolean insertMedicine(Medicine medicine) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, medicine.getName());
        values.put(COLUMN_DOSAGE, medicine.getDosage());
        values.put(COLUMN_TIME, medicine.getTime());
        values.put(COLUMN_PILL_COUNT, medicine.getPillCount());
        values.put(COLUMN_EXPIRY_DATE, medicine.getExpiryDate());
        values.put(COLUMN_USER_EMAIL, medicine.getUserEmail());

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    public ArrayList<Medicine> getAllMedicines(String userEmail) {
        ArrayList<Medicine> medicineList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME +
                        " WHERE " + COLUMN_USER_EMAIL + "=?",
                new String[]{userEmail}
        );

        if (cursor.moveToFirst()) {
            do {
                Medicine medicine = new Medicine(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getString(5),
                        cursor.getString(6)
                );

                medicineList.add(medicine);

            } while (cursor.moveToNext());
        }

        cursor.close();
        return medicineList;
    }

    public boolean updateMedicine(Medicine medicine) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, medicine.getName());
        values.put(COLUMN_DOSAGE, medicine.getDosage());
        values.put(COLUMN_TIME, medicine.getTime());
        values.put(COLUMN_PILL_COUNT, medicine.getPillCount());
        values.put(COLUMN_EXPIRY_DATE, medicine.getExpiryDate());
        values.put(COLUMN_USER_EMAIL, medicine.getUserEmail());

        int result = db.update(
                TABLE_NAME,
                values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(medicine.getId())}
        );

        return result > 0;
    }

    public boolean deleteMedicine(int id) {
        SQLiteDatabase db = getWritableDatabase();

        int result = db.delete(
                TABLE_NAME,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }

    public boolean updatePillCount(int id, int newCount) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PILL_COUNT, newCount);

        int result = db.update(
                TABLE_NAME,
                values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }

    public boolean insertHistory(String medicineName, String status, String time, String userEmail) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(HISTORY_MEDICINE_NAME, medicineName);
        values.put(HISTORY_STATUS, status);
        values.put(HISTORY_TIME, time);
        values.put(HISTORY_USER_EMAIL, userEmail);

        long result = db.insert(HISTORY_TABLE, null, values);
        return result != -1;
    }

    public ArrayList<String> getAllHistory(String userEmail) {
        ArrayList<String> historyList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + HISTORY_TABLE +
                        " WHERE " + HISTORY_USER_EMAIL + "=? " +
                        " ORDER BY " + HISTORY_ID + " DESC",
                new String[]{userEmail}
        );

        if (cursor.moveToFirst()) {
            do {
                String medicineName = cursor.getString(1);
                String status = cursor.getString(2);
                String time = cursor.getString(3);

                historyList.add(medicineName + " - " + status + " - " + time);

            } while (cursor.moveToNext());
        }

        cursor.close();
        return historyList;
    }
}