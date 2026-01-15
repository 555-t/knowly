package com.example.knowly;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private TextView btnSignUp, btnGoToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressDialog pd;

    // PASSWORD RULES:
    // - At least 6 characters
    // - At least 1 number
    // - At least 1 letter (upper or lower, doesn't matter)
    // - At least 1 special character (@#$%^&+=!)
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +             // at least 1 digit
                    "(?=.*[a-zA-Z])" +          // at least 1 letter (any case)
                    "(?=.*[@#$%^&+=!])" +       // at least 1 special character
                    "(?=\\S+$)" +               // no white spaces
                    ".{6,}" +                   // at least 6 characters
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 1. Bind Views
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        // 2. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        pd = new ProgressDialog(this);

        // 3. Navigation to Login
        btnGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // 4. Register Button Click
        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validateInputs(email, username, password, confirmPassword)) {
                checkUsernameAndRegister(username, email, password);
            }
        });
    }

    private boolean validateInputs(String email, String username, String password, String confirm) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate Email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email address");
            etEmail.requestFocus();
            return false;
        }

        // Validate Password Strength
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            etPassword.setError("Password too weak. Needs 6+ chars, 1 number, 1 letter, and 1 symbol (@#$%).");
            etPassword.requestFocus();
            return false;
        }

        // Validate Confirm Password
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void checkUsernameAndRegister(String username, String email, String password) {
        pd.setMessage("Checking username availability...");
        pd.setCancelable(false);
        pd.show();

        // Check Firestore for duplicate username
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null && !snapshot.isEmpty()) {
                            // Username taken
                            pd.dismiss();
                            etUsername.setError("Username taken! Please choose another.");
                            etUsername.requestFocus();
                        } else {
                            // Username available -> Create Account
                            createAuthAccount(username, email, password);
                        }
                    } else {
                        pd.dismiss();
                        Toast.makeText(this, "Network error checking username.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createAuthAccount(String username, String email, String password) {
        pd.setMessage("Sending Verification Email...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    pd.dismiss();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Send verification email
                            user.sendEmailVerification();

                            // Show the Dialog
                            showVerificationDialog(user, username, email);
                        }
                    } else {
                        Toast.makeText(this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showVerificationDialog(FirebaseUser user, String username, String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify your Email");

        // --- SPAM WARNING ADDED HERE ---
        builder.setMessage("We sent a link to " + email + ".\n\nPlease check your Inbox (and Spam/Junk folder!) to verify.");

        builder.setCancelable(false); // Force them to interact with buttons

        // 1. VERIFY BUTTON
        builder.setPositiveButton("I Have Verified", (dialog, which) -> {
            checkVerificationStatus(user, username, email);
        });

        // 2. RESEND BUTTON
        builder.setNeutralButton("Resend", (dialog, which) -> {
            user.sendEmailVerification();
            Toast.makeText(this, "Email resent! Check Spam folder too.", Toast.LENGTH_SHORT).show();
            // Show dialog again so they are still blocked
            showVerificationDialog(user, username, email);
        });

        // 3. CHANGE EMAIL BUTTON
        builder.setNegativeButton("Change Email", (dialog, which) -> {
            if (user != null) {
                // Delete the temp account so they can try again with the correct email
                user.delete();
            }
            dialog.dismiss();
        });

        builder.show();
    }

    private void checkVerificationStatus(FirebaseUser user, String username, String email) {
        pd.setMessage("Verifying...");
        pd.show();

        // Important: Reload user to get fresh status from Firebase
        user.reload().addOnCompleteListener(task -> {
            if (user.isEmailVerified()) {
                // Success! Save to Firestore
                saveUserToFirestore(user.getUid(), username, email);
            } else {
                pd.dismiss();
                Toast.makeText(this, "Email not verified yet. Please try again.", Toast.LENGTH_SHORT).show();
                // Re-open dialog
                showVerificationDialog(user, username, email);
            }
        });
    }

    private void saveUserToFirestore(String userId, String username, String email) {
        pd.setMessage("Setting up profile...");

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", userId);
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("bio", "New User");
        userMap.put("credentials", "Student");
        userMap.put("interests", null); // Placeholder for next screen

        db.collection("users").document(userId)
                .set(userMap)
                .addOnCompleteListener(task -> {
                    pd.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();

                        // Proceed to Choose Interests
                        Intent intent = new Intent(RegisterActivity.this, ChooseInterestsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to save profile. Please contact support.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
