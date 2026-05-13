package com.example.smartmedicinemanager;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.Calendar;

public class AddMedicineActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private EditText etMedicineName, etDosage, etTime, etPillCount, etExpiryDate;
    private int medicineId = -1;

    private ActivityResultLauncher<String> pickImageLauncher;

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
        Button btnSave = findViewById(R.id.btnSaveMedicine);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        runTextRecognition(uri);
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
            btnSave.setText("Update Medicine");
        }

        etTime.setOnClickListener(v -> showTimePicker());
        etExpiryDate.setOnClickListener(v -> showDatePicker());

        btnScanOCR.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> saveOrUpdateMedicine());
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

        Medicine medicine = new Medicine(medicineId, name, dosage, time, pills, expiry);

        boolean success;
        if (medicineId == -1) {
            success = databaseHelper.insertMedicine(medicine);
        } else {
            success = databaseHelper.updateMedicine(medicine);
        }

        if (success) {
            scheduleMedicineReminder(name, time);

            if (!expiry.isEmpty()) {
                scheduleExpiryAlert(name, expiry);
            }

            Toast.makeText(
                    this,
                    medicineId == -1 ? "Medicine saved successfully" : "Medicine updated successfully",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        } else {
            Toast.makeText(this, "Operation failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void runTextRecognition(Uri uri) {
        try {
            InputImage image = InputImage.fromFilePath(this, uri);

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
                                if (lower.contains("mg") || lower.contains("ml") ||
                                        lower.contains("tablet") || lower.contains("capsule")) {
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

        } catch (Exception e) {
            Toast.makeText(this, "Unable to read image", Toast.LENGTH_SHORT).show();
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

    private void scheduleExpiryAlert(String medicineName, String expiryDate) {
        String[] parts = expiryDate.split("/");
        if (parts.length != 3) return;

        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1;
        int year = Integer.parseInt(parts[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        calendar.add(Calendar.DAY_OF_MONTH, -3);

        if (calendar.before(Calendar.getInstance())) {
            return;
        }

        Intent intent = new Intent(this, ExpiryReceiver.class);
        intent.putExtra("medicineName", medicineName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) (System.currentTimeMillis() + 1),
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
            Toast.makeText(this, "Expiry alarm not allowed", Toast.LENGTH_SHORT).show();
        }
    }
}