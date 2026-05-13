package com.example.smartmedicinemanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardView cardReminder = findViewById(R.id.cardReminder);
        CardView cardMedicines = findViewById(R.id.cardMedicines);
        CardView cardHistory = findViewById(R.id.cardHistory);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_home);

        cardReminder.setOnClickListener(v ->
                startActivity(new Intent(this, AddMedicineActivity.class))
        );

        cardMedicines.setOnClickListener(v ->
                startActivity(new Intent(this, ViewMedicinesActivity.class))
        );

        cardHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class))
        );

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(this, AddMedicineActivity.class));
                return true;
            } else if (id == R.id.nav_medicines) {
                startActivity(new Intent(this, ViewMedicinesActivity.class));
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }
}
