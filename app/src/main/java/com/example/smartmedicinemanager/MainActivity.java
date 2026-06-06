package com.example.smartmedicinemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    TextView txtWelcomeName;

    CardView cardReminder;
    CardView cardMedicines;
    CardView cardHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Welcome Name
        txtWelcomeName = findViewById(R.id.txtWelcomeName);

        String fullName = getSharedPreferences("UserData", MODE_PRIVATE)
                .getString("fullName", "User");

        txtWelcomeName.setText("Hello, " + fullName);

        // Cards
        cardReminder = findViewById(R.id.cardReminder);
        cardMedicines = findViewById(R.id.cardMedicines);
        cardHistory = findViewById(R.id.cardHistory);

        // Set Reminder Card
        cardReminder.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this,
                        AddMedicineActivity.class))
        );

        // View Medicines Card
        cardMedicines.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this,
                        ViewMedicinesActivity.class))
        );

        // History Card
        cardHistory.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this,
                        HistoryActivity.class))
        );

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            // Home
            if (id == R.id.nav_home) {

                return true;
            }

            // Add Medicine
            else if (id == R.id.nav_add) {

                startActivity(new Intent(MainActivity.this,
                        AddMedicineActivity.class));

                return true;
            }

            // View Medicines
            else if (id == R.id.nav_medicine) {

                startActivity(new Intent(MainActivity.this,
                        ViewMedicinesActivity.class));

                return true;
            }

            // History
            else if (id == R.id.nav_history) {

                startActivity(new Intent(MainActivity.this,
                        HistoryActivity.class));

                return true;
            }

            // Profile
            else if (id == R.id.nav_profile) {

                startActivity(new Intent(MainActivity.this,
                        ProfileActivity.class));

                return true;
            }

            return false;
        });
    }
}