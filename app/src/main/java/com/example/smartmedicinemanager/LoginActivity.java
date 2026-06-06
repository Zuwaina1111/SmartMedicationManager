package com.example.smartmedicinemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginEmail;
    private EditText etLoginPassword;
    private Button btnLogin;
    private TextView txtSignUp;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);

        etLoginEmail = findViewById(R.id.etEmail);
        etLoginPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        txtSignUp = findViewById(R.id.txtSignUp);

        btnLogin.setOnClickListener(v -> {

            String email = etLoginEmail.getText().toString().trim();
            String password = etLoginPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(this,
                        "Please enter email and password",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            boolean exists = databaseHelper.checkUser(email, password);

            if (exists) {

                Toast.makeText(this,
                        "Login Successful",
                        Toast.LENGTH_SHORT).show();

                startActivity(new Intent(
                        LoginActivity.this,
                        MainActivity.class));

                finish();

            } else {

                Toast.makeText(this,
                        "Invalid email or password",
                        Toast.LENGTH_SHORT).show();
            }
        });

        txtSignUp.setOnClickListener(v ->
                startActivity(new Intent(
                        LoginActivity.this,
                        RegisterActivity.class))
        );
    }
}