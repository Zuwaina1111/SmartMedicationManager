package com.example.smartmedicinemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "medicines";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DOSAGE = "dosage";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_PILL_COUNT = "pill_count";
    public static final String COLUMN_EXPIRY_DATE = "expiry_date";

    public static final String HISTORY_TABLE = "history";
    public static final String HISTORY_ID = "history_id";
    public static final String HISTORY_MEDICINE_NAME = "medicine_name";
    public static final String HISTORY_STATUS = "status";
    public static final String HISTORY_TIME = "time_taken";

    public DatabaseHelper(Context context) {
        super(context, "MedicineDB", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_DOSAGE + " TEXT, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_PILL_COUNT + " INTEGER, " +
                COLUMN_EXPIRY_DATE + " TEXT)");

        db.execSQL("CREATE TABLE " + HISTORY_TABLE + " (" +
                HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                HISTORY_MEDICINE_NAME + " TEXT, " +
                HISTORY_STATUS + " TEXT, " +
                HISTORY_TIME + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
        onCreate(db);
    }

    public boolean insertMedicine(Medicine medicine) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, medicine.getName());
        values.put(COLUMN_DOSAGE, medicine.getDosage());
        values.put(COLUMN_TIME, medicine.getTime());
        values.put(COLUMN_PILL_COUNT, medicine.getPillCount());
        values.put(COLUMN_EXPIRY_DATE, medicine.getExpiryDate());

        long result = db.insert(TABLE_NAME, null, values);
        db.close();
        return result != -1;
    }

    public ArrayList<Medicine> getAllMedicines() {
        ArrayList<Medicine> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                Medicine medicine = new Medicine(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOSAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PILL_COUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRY_DATE))
                );
                list.add(medicine);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    public boolean updateMedicine(Medicine medicine) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, medicine.getName());
        values.put(COLUMN_DOSAGE, medicine.getDosage());
        values.put(COLUMN_TIME, medicine.getTime());
        values.put(COLUMN_PILL_COUNT, medicine.getPillCount());
        values.put(COLUMN_EXPIRY_DATE, medicine.getExpiryDate());

        int result = db.update(
                TABLE_NAME,
                values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(medicine.getId())}
        );

        db.close();
        return result > 0;
    }

    public boolean deleteMedicine(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(
                TABLE_NAME,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        db.close();
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

        db.close();
        return result > 0;
    }

    public boolean insertHistory(String medicineName, String status, String time) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(HISTORY_MEDICINE_NAME, medicineName);
        values.put(HISTORY_STATUS, status);
        values.put(HISTORY_TIME, time);

        long result = db.insert(HISTORY_TABLE, null, values);
        db.close();
        return result != -1;
    }

    public ArrayList<String> getAllHistory() {
        ArrayList<String> historyList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + HISTORY_TABLE + " ORDER BY " + HISTORY_ID + " DESC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                String medicineName = cursor.getString(cursor.getColumnIndexOrThrow(HISTORY_MEDICINE_NAME));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(HISTORY_STATUS));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(HISTORY_TIME));

                historyList.add(medicineName + " - " + status + " - " + time);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return historyList;
    }
}
