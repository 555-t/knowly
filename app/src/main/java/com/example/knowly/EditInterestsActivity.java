package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class EditInterestsActivity extends AppCompatActivity {

    private ChipGroup chipGroupSelection;
    private Button btnCancel, btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure this matches your exact XML filename (you had a typo in the file name provided earlier)
        setContentView(R.layout.activity_editinterests);

        // 1. Initialize Views
        chipGroupSelection = findViewById(R.id.chip_group_selection);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm_interests);

        // 2. Cancel Button Logic
        btnCancel.setOnClickListener(v -> finish());

        // 3. Confirm (Done) Button Logic
        btnConfirm.setOnClickListener(v -> {
            ArrayList<String> selectedInterests = new ArrayList<>();

            // Loop through all chips in the group
            for (int i = 0; i < chipGroupSelection.getChildCount(); i++) {
                // Get the child view and cast it to a Chip
                Chip chip = (Chip) chipGroupSelection.getChildAt(i);

                // If the user selected this chip, add its text to our list
                if (chip.isChecked()) {
                    selectedInterests.add(chip.getText().toString());
                }
            }

            // Prepare the result to send back
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra("selected_interests", selectedInterests);

            // Set result as OK so EditProfileActivity knows it worked
            setResult(RESULT_OK, resultIntent);

            // Close the screen
            finish();
        });
    }
}
