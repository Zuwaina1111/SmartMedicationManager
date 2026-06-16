package com.example.smartmedicinemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class ViewMedicinesActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private ListView listView;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_medicines);

        databaseHelper = new DatabaseHelper(this);
        listView = findViewById(R.id.listViewMedicines);

        userEmail = getSharedPreferences("UserData", MODE_PRIVATE)
                .getString("email", "");

        loadMedicines();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        userEmail = getSharedPreferences("UserData", MODE_PRIVATE)
                .getString("email", "");

        loadMedicines();
    }

    private void loadMedicines() {
        List<Medicine> medicines = databaseHelper.getAllMedicines(userEmail);
        listView.setAdapter(new MedicineAdapter(medicines));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_medicine);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(ViewMedicinesActivity.this, MainActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_add) {
                startActivity(new Intent(ViewMedicinesActivity.this, AddMedicineActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_medicine) {
                return true;

            } else if (id == R.id.nav_history) {
                startActivity(new Intent(ViewMedicinesActivity.this, HistoryActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(ViewMedicinesActivity.this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private class MedicineAdapter extends BaseAdapter {

        private final List<Medicine> medicines;

        MedicineAdapter(List<Medicine> medicines) {
            this.medicines = medicines;
        }

        @Override
        public int getCount() {
            return medicines.size();
        }

        @Override
        public Object getItem(int position) {
            return medicines.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(ViewMedicinesActivity.this)
                    .inflate(R.layout.medicine_item, parent, false);

            Medicine medicine = medicines.get(position);

            TextView txtName = view.findViewById(R.id.txtName);
            TextView txtDosage = view.findViewById(R.id.txtDosage);
            TextView txtTime = view.findViewById(R.id.txtTime);
            TextView txtPills = view.findViewById(R.id.txtPills);
            TextView txtExpiry = view.findViewById(R.id.txtExpiry);

            Button btnDelete = view.findViewById(R.id.btnDeleteMedicine);
            Button btnEdit = view.findViewById(R.id.btnEditMedicine);

            txtName.setText(medicine.getName());
            txtDosage.setText("Dosage: " + medicine.getDosage());
            txtTime.setText("Time: " + medicine.getTime());
            txtPills.setText("Pills Left: " + medicine.getPillCount());

            if (medicine.getExpiryDate() == null || medicine.getExpiryDate().isEmpty()) {
                txtExpiry.setText("Expiry: Not set");
            } else {
                txtExpiry.setText("Expiry: " + medicine.getExpiryDate());
            }

            btnDelete.setOnClickListener(v -> {
                databaseHelper.deleteMedicine(medicine.getId());
                loadMedicines();
            });

            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(ViewMedicinesActivity.this, AddMedicineActivity.class);

                intent.putExtra("id", medicine.getId());
                intent.putExtra("name", medicine.getName());
                intent.putExtra("dosage", medicine.getDosage());
                intent.putExtra("time", medicine.getTime());
                intent.putExtra("pillCount", medicine.getPillCount());
                intent.putExtra("expiryDate", medicine.getExpiryDate());
                intent.putExtra("userEmail", medicine.getUserEmail());

                startActivity(intent);
            });

            return view;
        }
    }
}