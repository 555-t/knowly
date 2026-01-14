package com.example.knowly;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Buttons from your XML
        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnSave = findViewById(R.id.btn_save);

        // Cancel Button: Closes this page and goes back
        btnCancel.setOnClickListener(v -> finish());

        // Save Button: Just a placeholder for now
        btnSave.setOnClickListener(v -> {
            Toast.makeText(this, "Changes Saved!", Toast.LENGTH_SHORT).show();
            // TODO: Add logic to save data to Firebase/Database here
            finish(); // Close page after save
        });
    }
}