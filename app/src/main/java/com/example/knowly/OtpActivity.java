package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OtpActivity extends AppCompatActivity {

    EditText etOtp;
    Button btnVerify;

    // Hardcoded OTP for testing
    private final String TEST_OTP = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_otp);

        etOtp = findViewById(R.id.etOtp);
        btnVerify = findViewById(R.id.btnVerify);

        btnVerify.setOnClickListener(v -> {
            String enteredOtp = etOtp.getText().toString().trim();

            if(enteredOtp.isEmpty()){
                Toast.makeText(OtpActivity.this, "Enter OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            if(enteredOtp.equals(TEST_OTP)){
                Toast.makeText(OtpActivity.this, "OTP Verified!", Toast.LENGTH_SHORT).show();

                // Go to Reset Password screen
                Intent intent = new Intent(OtpActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(OtpActivity.this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
