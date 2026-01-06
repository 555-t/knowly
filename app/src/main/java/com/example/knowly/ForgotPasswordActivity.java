package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText etEmail;
    Button btnSendCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnSendCode = findViewById(R.id.btnVerify);

        btnSendCode.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if(email.isEmpty()){
                Toast.makeText(ForgotPasswordActivity.this, "Enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(ForgotPasswordActivity.this, "Code sent to " + email, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ForgotPasswordActivity.this, OtpActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
        });
    }
}
