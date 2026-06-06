package com.example.smartmedicinemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private Medicine currentMedicine;
    private ListView listViewHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        databaseHelper = new DatabaseHelper(this);

        TextView txtMedicineName = findViewById(R.id.txtMedicineName);
        TextView txtMedicineTime = findViewById(R.id.txtMedicineTime);
        Button btnTaken = findViewById(R.id.btnTaken);
        Button btnMissed = findViewById(R.id.btnMissed);
        listViewHistory = findViewById(R.id.listViewHistory);

        List<Medicine> medicines = databaseHelper.getAllMedicines();

        if (!medicines.isEmpty()) {
            currentMedicine = medicines.get(0);
            txtMedicineName.setText(currentMedicine.getName());
            txtMedicineTime.setText("Time: " + currentMedicine.getTime());
        } else {
            txtMedicineName.setText("No medicine found");
            txtMedicineTime.setText("Time: --");
        }

        loadHistory();

        btnTaken.setOnClickListener(v -> {
            if (currentMedicine != null) {
                int newCount = currentMedicine.getPillCount() > 0
                        ? currentMedicine.getPillCount() - 1
                        : 0;

                databaseHelper.updatePillCount(currentMedicine.getId(), newCount);
                databaseHelper.insertHistory(currentMedicine.getName(), "Taken", getCurrentTime());

                Toast.makeText(this, "Marked as Taken", Toast.LENGTH_SHORT).show();
                loadHistory();
            }
        });

        btnMissed.setOnClickListener(v -> {
            if (currentMedicine != null) {
                databaseHelper.insertHistory(currentMedicine.getName(), "Missed", getCurrentTime());

                Toast.makeText(this, "Marked as Missed", Toast.LENGTH_SHORT).show();
                loadHistory();
            }
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_history);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(HistoryActivity.this, MainActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_add) {
                startActivity(new Intent(HistoryActivity.this, AddMedicineActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_medicine) {
                startActivity(new Intent(HistoryActivity.this, ViewMedicinesActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_history) {
                return true;

            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HistoryActivity.this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadHistory() {
        List<String> historyList = databaseHelper.getAllHistory();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                historyList
        );

        listViewHistory.setAdapter(adapter);
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}