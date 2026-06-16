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
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private ListView listViewHistory;
    private TextView txtMedicineName, txtMedicineTime;
    private Button btnTaken, btnMissed;

    private String medicineName = "Medicine";
    private String medicineTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        databaseHelper = new DatabaseHelper(this);

        listViewHistory = findViewById(R.id.listViewHistory);
        txtMedicineName = findViewById(R.id.txtMedicineName);
        txtMedicineTime = findViewById(R.id.txtMedicineTime);
        btnTaken = findViewById(R.id.btnTaken);
        btnMissed = findViewById(R.id.btnMissed);

        String nameExtra = getIntent().getStringExtra("name");
        String timeExtra = getIntent().getStringExtra("time");

        if (nameExtra != null && !nameExtra.isEmpty()) medicineName = nameExtra;
        if (timeExtra != null) medicineTime = timeExtra;

        txtMedicineName.setText(medicineName);
        txtMedicineTime.setText("Time: " + medicineTime);

        btnTaken.setOnClickListener(v -> saveStatus("Taken"));
        btnMissed.setOnClickListener(v -> saveStatus("Missed"));

        loadHistory();
        setupBottomNavigation();
    }

    private void saveStatus(String status) {
        String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        String userEmail = getSharedPreferences("UserData", MODE_PRIVATE)
                .getString("email", "");

        boolean inserted = databaseHelper.insertHistory(
                medicineName,
                status,
                currentTime,
                userEmail
        );

        if (inserted) {
            Toast.makeText(this, "Saved as " + status, Toast.LENGTH_SHORT).show();
            loadHistory();
        } else {
            Toast.makeText(this, "Failed to save history", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadHistory() {
        String userEmail = getSharedPreferences("UserData", MODE_PRIVATE)
                .getString("email", "");

        ArrayList<String> historyList = databaseHelper.getAllHistory(userEmail);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                historyList
        );

        listViewHistory.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_history);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(this, AddMedicineActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_medicine) {
                startActivity(new Intent(this, ViewMedicinesActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_history) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }
}