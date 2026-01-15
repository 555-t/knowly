package com.example.knowly;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private ChipGroup chipGroup;
    private TextInputLayout inputUsername, inputBio, inputCredentials;
    private Button btnCancel, btnSave;
    private TextView btnEditInterests;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ArrayList<String> currentInterests = new ArrayList<>();
    private List<String> masterCategoryList = new ArrayList<>();
    private boolean isEditingInterests = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        inputUsername = findViewById(R.id.input_username);
        inputBio = findViewById(R.id.input_bio);
        inputCredentials = findViewById(R.id.input_credentials);
        chipGroup = findViewById(R.id.chip_group);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        btnEditInterests = findViewById(R.id.btn_edit_interests);

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfileToFirebase());

        btnEditInterests.setOnClickListener(v -> {
            isEditingInterests = !isEditingInterests;
            if (isEditingInterests) {
                btnEditInterests.setText("Done");
                btnEditInterests.setTextColor(Color.parseColor("#4CAF50"));
                fetchMasterListAndDisplay();
            } else {
                btnEditInterests.setText("Edit");
                btnEditInterests.setTextColor(Color.parseColor("#00BCD4"));
                updateInterestsDisplay(currentInterests);
            }
        });

        loadCurrentUserData();
    }

    private void fetchMasterListAndDisplay() {
        // Points to categories -> allCategories -> list as seen in your screenshot
        db.collection("categories").document("allCategories")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> fetchedList = (List<String>) documentSnapshot.get("list");
                        if (fetchedList != null) {
                            masterCategoryList = new ArrayList<>(fetchedList);

                            // CUSTOM SORT: Selected items first, then alphabetical
                            Collections.sort(masterCategoryList, (s1, s2) -> {
                                boolean b1 = currentInterests.contains(s1);
                                boolean b2 = currentInterests.contains(s2);
                                if (b1 && !b2) return -1;
                                if (!b1 && b2) return 1;
                                return s1.compareTo(s2);
                            });

                            displayAllChipsAsSelectable();
                        }
                    }
                });
    }

    private void displayAllChipsAsSelectable() {
        chipGroup.removeAllViews();
        for (String category : masterCategoryList) {
            Chip chip = new Chip(this);
            chip.setText(category);

            // Apply selected vs unselected styling
            if (currentInterests.contains(category)) {
                // Style: Selected (Matches the teal look)
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E0F2F1")));
                chip.setTextColor(Color.parseColor("#00695C"));
                chip.setChipStrokeWidth(0f);
            } else {
                // Style: Unselected (White with grey border)
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                chip.setTextColor(Color.parseColor("#757575"));
                chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
                chip.setChipStrokeWidth(2f);
            }

            chip.setOnClickListener(v -> {
                if (currentInterests.contains(category)) {
                    currentInterests.remove(category);
                } else {
                    currentInterests.add(category);
                }
                // Redraw immediately to show the color change
                displayAllChipsAsSelectable();
            });

            chipGroup.addView(chip);
        }
    }

    private void loadCurrentUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (inputUsername.getEditText() != null)
                            inputUsername.getEditText().setText(documentSnapshot.getString("username"));
                        if (inputBio.getEditText() != null)
                            inputBio.getEditText().setText(documentSnapshot.getString("bio"));
                        if (inputCredentials.getEditText() != null)
                            inputCredentials.getEditText().setText(documentSnapshot.getString("credentials"));

                        List<String> savedInterests = (List<String>) documentSnapshot.get("interests");
                        if (savedInterests != null) {
                            updateInterestsDisplay(new ArrayList<>(savedInterests));
                        }
                    }
                });
    }

    private void saveProfileToFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> profile = new HashMap<>();
        profile.put("username", inputUsername.getEditText().getText().toString().trim());
        profile.put("bio", inputBio.getEditText().getText().toString().trim());
        profile.put("credentials", inputCredentials.getEditText().getText().toString().trim());
        profile.put("interests", currentInterests);

        db.collection("users").document(user.getUid())
                .set(profile, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    com.google.firebase.database.FirebaseDatabase.getInstance()
                            .getReference("Users").child(user.getUid())
                            .child("interests").setValue(currentInterests);
                    Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateInterestsDisplay(ArrayList<String> interests) {
        this.currentInterests = interests;
        chipGroup.removeAllViews();
        for (String interest : interests) {
            Chip chip = new Chip(this);
            chip.setText(interest);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E0F2F1")));
            chip.setTextColor(Color.parseColor("#00695C"));
            chip.setCloseIconVisible(true);
            chip.setCloseIconTint(ColorStateList.valueOf(Color.parseColor("#00695C")));
            chip.setOnCloseIconClickListener(v -> {
                currentInterests.remove(interest);
                updateInterestsDisplay(currentInterests);
            });
            chipGroup.addView(chip);
        }
    }
}