package com.example.knowly;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

// Firebase Imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    // Views
    private ChipGroup chipGroup;
    private TextInputLayout inputUsername, inputBio, inputCredentials;
    private Button btnCancel, btnSave;
    private TextView btnEditInterests;

    // Firebase Variables
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

        // 2. Initialize Views (IDs must match your activity_edit_profile.xml)
        inputUsername = findViewById(R.id.input_username);
        inputBio = findViewById(R.id.input_bio);
        inputCredentials = findViewById(R.id.input_credentials);
        chipGroup = findViewById(R.id.chip_group); // Ensure your ChipGroup has this ID in XML!

        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        btnEditInterests = findViewById(R.id.btn_edit_interests); // Ensure your "Edit" text has this ID

        // 3. Register the result launcher (To catch data coming back from EditInterestsActivity)
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

        // --- SAVE BUTTON: Logic to save to Firebase ---
        btnSave.setOnClickListener(v -> saveProfileToFirebase());

        // Open Edit Interests Page
        btnEditInterests.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, EditInterestsActivity.class);
            editInterestsLauncher.launch(intent);
        });

        // (Optional) If you want to load existing data when opening the page,
        // you would call a loadData() function here.
    }

    private void saveProfileToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // DEBUG 1: Check if user is logged in
        if (currentUser == null) {
            Log.e("KnowlyDebug", "ERROR: User is null! You are not logged in.");
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        Log.d("KnowlyDebug", "User Found. UID: " + uid);

        // ... (Your code to get inputs remains the same) ...
        // A. Get Data from Input Fields safely
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

        // B. Get Data from Chips (Interests)
        ArrayList<String> interestsList = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            interestsList.add(chip.getText().toString());
        }

        // DEBUG 2: Print data to be saved
        Log.d("KnowlyDebug", "Attempting to save: " + username + ", " + bio);

        // C. Create the Data Map
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("username", username);
        userProfile.put("bio", bio);
        userProfile.put("credentials", credentials);
        userProfile.put("interests", interestsList);

        // D. Save to Firestore
        db.collection("users").document(uid)
                .set(userProfile, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // DEBUG 3: Success!
                    Log.d("KnowlyDebug", "SUCCESS! Data written to Firestore.");
                    Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // DEBUG 4: Failure!
                    Log.e("KnowlyDebug", "FAILURE! Could not save: " + e.getMessage());
                    Toast.makeText(this, "Save Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Helper to redraw chips on the screen
    private void updateInterestsDisplay(ArrayList<String> interests) {
        chipGroup.removeAllViews();
        for (String interest : interests) {
            Chip chip = new Chip(this);
            chip.setText(interest);
            // Styling the chips to look like your design
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E0F2F1")));
            chip.setTextColor(Color.parseColor("#00695C"));
            chip.setCloseIconVisible(true);
            chip.setCloseIconTint(ColorStateList.valueOf(Color.parseColor("#00695C")));
            chipGroup.addView(chip);
        }
    }
}

