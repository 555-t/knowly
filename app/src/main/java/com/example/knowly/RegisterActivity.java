package com.example.knowly;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private TextView btnSignUp, btnGoToLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Bind views
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        pd = new ProgressDialog(this);

        // Go back to login
        btnGoToLogin.setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class))
        );

        // Register user
        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username)
                    || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {

                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();

            } else if (!password.equals(confirmPassword)) {

                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();

            } else if (password.length() < 6) {

                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();

            } else {
                registerUser(username, email, password);
            }
        });
    }

    private void registerUser(String username, String email, String password) {

        pd.setMessage("Creating account...");
        pd.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String userId = mAuth.getCurrentUser().getUid();

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", userId);
                    map.put("username", username);
                    map.put("email", email);
                    map.put("bio", "");
                    map.put("imageurl", "default");

                    mRootRef.child("Users").child(userId)
                            .setValue(map)
                            .addOnCompleteListener(task -> {
                                pd.dismiss();

                                if (task.isSuccessful()) {
                                    Toast.makeText(this,
                                            "Account created successfully",
                                            Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
