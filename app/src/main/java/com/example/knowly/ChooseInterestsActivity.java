package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashSet;
import java.util.Set;

public class ChooseInterestsActivity extends AppCompatActivity {

    private final Set<String> selectedInterests = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_interest);

        // Setup cards
        setupCard(R.id.mathCard, "Mathematics");
        setupCard(R.id.scienceCard, "Science");
        setupCard(R.id.historyCard, "History");
        setupCard(R.id.literatureCard, "Literature");
        setupCard(R.id.csCard, "Computer Science");
        setupCard(R.id.artCard, "Art");
        setupCard(R.id.musicCard, "Music");
        setupCard(R.id.philosophyCard, "Philosophy");
        setupCard(R.id.psychologyCard, "Psychology");
        setupCard(R.id.biologyCard, "Biology");
        setupCard(R.id.chemistryCard, "Chemistry");
        setupCard(R.id.physicsCard, "Physics");
        setupCard(R.id.economicsCard, "Economics");
        setupCard(R.id.languagesCard, "Languages");
        setupCard(R.id.engineeringCard, "Engineering");
        setupCard(R.id.medicineCard, "Medicine");

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            if (selectedInterests.size() < 3) {
                Toast.makeText(this,
                        "Please select at least 3 interests",
                        Toast.LENGTH_SHORT).show();
            } else {
                saveInterestsAndProceed();
            }
        });
    }


    private void setupCard(int cardId, String interest) {
        MaterialCardView card = findViewById(cardId);

        card.setOnClickListener(v -> {
            card.setChecked(!card.isChecked());

            if (card.isChecked()) {
                selectedInterests.add(interest);
            } else {
                selectedInterests.remove(interest);
            }
        });
    }

    private void saveInterestsAndProceed() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Convert Set -> Map (Firebase-safe)
        java.util.HashMap<String, Boolean> interestsMap = new java.util.HashMap<>();
        for (String interest : selectedInterests) {
            interestsMap.put(interest, true);
        }

        FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .child("interests")
                .setValue(interestsMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Interests saved!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomePage.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Save failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}