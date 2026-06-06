package com.example.smartmedicinemanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtProfileName;
    private TextView txtProfileEmail;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        txtProfileName = findViewById(R.id.txtProfileName);
        txtProfileEmail = findViewById(R.id.txtProfileEmail);
        btnLogout = findViewById(R.id.btnLogout);

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        String fullName = prefs.getString("fullName", "User Name");
        String email = prefs.getString("email", "user@email.com");

        txtProfileName.setText(fullName);
        txtProfileEmail.setText(email);

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_add) {
                startActivity(new Intent(ProfileActivity.this, AddMedicineActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_medicine) {
                startActivity(new Intent(ProfileActivity.this, ViewMedicinesActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_history) {
                startActivity(new Intent(ProfileActivity.this, HistoryActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_profile) {
                return true;
            }

            return false;
        });
    }
}