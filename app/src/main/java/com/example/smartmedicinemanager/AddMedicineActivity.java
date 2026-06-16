package com.example.smartmedicinemanager;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.Calendar;

public class AddMedicineActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private EditText etMedicineName, etDosage, etTime, etPillCount, etExpiryDate;
    private int medicineId = -1;
    private ActivityResultLauncher<Void> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        databaseHelper = new DatabaseHelper(this);

        etMedicineName = findViewById(R.id.etMedicineName);
        etDosage = findViewById(R.id.etDosage);
        etTime = findViewById(R.id.etTime);
        etPillCount = findViewById(R.id.etPillCount);
        etExpiryDate = findViewById(R.id.etExpiryDate);

        Button btnScanOCR = findViewById(R.id.btnScanOCR);
        Button btnSaveMedicine = findViewById(R.id.btnSaveMedicine);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        runOCRFromBitmap(bitmap);
                    } else {
                        Toast.makeText(this, "No image captured", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        medicineId = getIntent().getIntExtra("id", -1);

        if (medicineId != -1) {
            etMedicineName.setText(getIntent().getStringExtra("name"));
            etDosage.setText(getIntent().getStringExtra("dosage"));
            etTime.setText(getIntent().getStringExtra("time"));
            etPillCount.setText(String.valueOf(getIntent().getIntExtra("pillCount", 0)));
            etExpiryDate.setText(getIntent().getStringExtra("expiryDate"));
            btnSaveMedicine.setText("Update Medicine");
        }

        etTime.setOnClickListener(v -> showTimePicker());
        etExpiryDate.setOnClickListener(v -> showDatePicker());

        btnScanOCR.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraLauncher.launch(null);
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            }
        });

        btnSaveMedicine.setOnClickListener(v -> saveOrUpdateMedicine());

        setupBottomNavigation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraLauncher.launch(null);
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void runOCRFromBitmap(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(result -> {
                    String text = result.getText().trim();

                    if (!text.isEmpty()) {
                        String[] lines = text.split("\\n");

                        if (lines.length > 0) {
                            etMedicineName.setText(lines[0]);
                        }

                        for (String line : lines) {
                            String lower = line.toLowerCase();

                            if (lower.contains("mg")
                                    || lower.contains("ml")
                                    || lower.contains("tablet")
                                    || lower.contains("capsule")) {

                                etDosage.setText(line);
                                break;
                            }
                        }

                        Toast.makeText(this, "OCR text extracted", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(this, "No text found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "OCR failed", Toast.LENGTH_SHORT).show()
                );
    }

    private void saveOrUpdateMedicine() {
        String name = etMedicineName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String pillsText = etPillCount.getText().toString().trim();
        String expiry = etExpiryDate.getText().toString().trim();

        if (name.isEmpty() || dosage.isEmpty() || time.isEmpty() || pillsText.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int pills;

        try {
            pills = Integer.parseInt(pillsText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid pill count", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = getSharedPreferences("UserData", MODE_PRIVATE)
                .getString("email", "");

        Medicine medicine = new Medicine(
                medicineId,
                name,
                dosage,
                time,
                pills,
                expiry,
                userEmail
        );

        boolean success;

        if (medicineId == -1) {
            success = databaseHelper.insertMedicine(medicine);
        } else {
            success = databaseHelper.updateMedicine(medicine);
        }

        if (success) {
            scheduleMedicineReminder(name, time);
            Toast.makeText(this, "Medicine saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Operation failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) ->
                        etTime.setText(String.format("%02d:%02d", hourOfDay, minute)),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        dialog.show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) ->
                        etExpiryDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void scheduleMedicineReminder(String medicineName, String time) {
        String[] parts = time.split(":");

        if (parts.length != 2) return;

        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("medicineName", medicineName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        try {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } catch (SecurityException e) {
            Toast.makeText(this, "Alarm permission not allowed", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_add);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(AddMedicineActivity.this, MainActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_add) {
                return true;

            } else if (id == R.id.nav_medicine) {
                startActivity(new Intent(AddMedicineActivity.this, ViewMedicinesActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_history) {
                startActivity(new Intent(AddMedicineActivity.this, HistoryActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(AddMedicineActivity.this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }
}