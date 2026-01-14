package com.example.knowly;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.DocumentSnapshot; // Needed for reading

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    // Views
    private ChipGroup chipGroup;
    private TextInputLayout inputUsername, inputBio, inputCredentials;
    private Button btnCancel, btnSave;
    private TextView btnEditInterests;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Launcher for the Interests page
    private ActivityResultLauncher<Intent> editInterestsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 1. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. Initialize Views
        inputUsername = findViewById(R.id.input_username);
        inputBio = findViewById(R.id.input_bio);
        inputCredentials = findViewById(R.id.input_credentials);
        chipGroup = findViewById(R.id.chip_group);

        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        btnEditInterests = findViewById(R.id.btn_edit_interests);

        // 3. Register Result Launcher
        editInterestsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> newInterests = result.getData().getStringArrayListExtra("selected_interests");
                        if (newInterests != null) {
                            updateInterestsDisplay(newInterests);
                        }
                    }
                }
        );

        // 4. Click Listeners
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfileToFirebase());
        btnEditInterests.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, EditInterestsActivity.class);
            editInterestsLauncher.launch(intent);
        });

        // 5. NEW: Load existing data immediately!
        loadCurrentUserData();
    }

    // --- NEW FUNCTION: READS DATA FROM DB ---
    private void loadCurrentUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Clear default chips so we don't see duplicates
        chipGroup.removeAllViews();

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // A. Fill Text Fields
                        String username = documentSnapshot.getString("username");
                        String bio = documentSnapshot.getString("bio");
                        String credentials = documentSnapshot.getString("credentials");

                        if (inputUsername.getEditText() != null) inputUsername.getEditText().setText(username);
                        if (inputBio.getEditText() != null) inputBio.getEditText().setText(bio);
                        if (inputCredentials.getEditText() != null) inputCredentials.getEditText().setText(credentials);

                        // B. Fill Interests Chips
                        // Firestore stores arrays as "List<String>"
                        List<String> savedInterests = (List<String>) documentSnapshot.get("interests");
                        if (savedInterests != null) {
                            // Convert List to ArrayList for our helper function
                            updateInterestsDisplay(new ArrayList<>(savedInterests));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Could not load profile data", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfileToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        String username = "";
        if (inputUsername.getEditText() != null) {
            username = inputUsername.getEditText().getText().toString().trim();
        }

        String bio = "";
        if (inputBio.getEditText() != null) {
            bio = inputBio.getEditText().getText().toString().trim();
        }

        String credentials = "";
        if (inputCredentials.getEditText() != null) {
            credentials = inputCredentials.getEditText().getText().toString().trim();
        }

        ArrayList<String> interestsList = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            interestsList.add(chip.getText().toString());
        }

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("username", username);
        userProfile.put("bio", bio);
        userProfile.put("credentials", credentials);
        userProfile.put("interests", interestsList);

        db.collection("users").document(uid)
                .set(userProfile, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Save Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateInterestsDisplay(ArrayList<String> interests) {
        chipGroup.removeAllViews();
        for (String interest : interests) {
            Chip chip = new Chip(this);
            chip.setText(interest);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E0F2F1")));
            chip.setTextColor(Color.parseColor("#00695C"));
            chip.setCloseIconVisible(true);
            chip.setCloseIconTint(ColorStateList.valueOf(Color.parseColor("#00695C")));
            chipGroup.addView(chip);
        }
    }
}