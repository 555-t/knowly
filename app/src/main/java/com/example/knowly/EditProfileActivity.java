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

import java.util.ArrayList;

public class EditProfileActivity extends AppCompatActivity {

    private ChipGroup chipGroup; // The group on the profile page
    private ActivityResultLauncher<Intent> editInterestsLauncher; // The listener

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 1. Initialize Views
        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnSave = findViewById(R.id.btn_save);
        TextView btnEditInterests = findViewById(R.id.btn_edit_interests);
        chipGroup = findViewById(R.id.chip_group); // Make sure your XML has this ID for the ChipGroup

        // 2. Define the "Launcher" to catch the returned data
        editInterestsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Get the list of strings
                        ArrayList<String> newInterests = result.getData().getStringArrayListExtra("selected_interests");
                        if (newInterests != null) {
                            updateInterestsDisplay(newInterests);
                        }
                    }
                }
        );

        // 3. Button Listeners
        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            // TODO: This is where you will eventually save to Firebase
            Toast.makeText(this, "Changes Saved!", Toast.LENGTH_SHORT).show();
            finish();
        });

        // 4. Open the interests page using the Launcher (NOT startActivity)
        btnEditInterests.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, EditInterestsActivity.class);
            editInterestsLauncher.launch(intent);
        });
    }

    // Helper function to rebuild the chips dynamically
    private void updateInterestsDisplay(ArrayList<String> interests) {
        // Clear existing chips
        chipGroup.removeAllViews();

        for (String interest : interests) {
            Chip chip = new Chip(this);
            chip.setText(interest);

            // Apply your styling (Teal colors)
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E0F2F1")));
            chip.setTextColor(Color.parseColor("#00695C"));
            chip.setCloseIconVisible(true);
            chip.setCloseIconTint(ColorStateList.valueOf(Color.parseColor("#00695C")));

            // Add to the group
            chipGroup.addView(chip);
        }
    }
}

