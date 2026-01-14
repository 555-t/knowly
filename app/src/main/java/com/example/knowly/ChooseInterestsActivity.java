package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
        ViewGroup bg = (ViewGroup) card.getChildAt(0);
        TextView text = (TextView) bg.getChildAt(0);
        ImageView checkIcon = card.findViewWithTag("checkIcon");

        card.setOnClickListener(v -> {
            boolean checked = !card.isChecked();
            card.setChecked(checked);

            bg.setSelected(checked);
            text.setSelected(checked);
            checkIcon.setVisibility(checked ? View.VISIBLE : View.GONE);

            card.setStrokeColor(
                    checked
                            ? ContextCompat.getColor(this, android.R.color.white)
                            : ContextCompat.getColor(this, R.color.dark_gray) // default
            );

            if (checked) {
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

        // Convert Set -> List
        java.util.List<String> interestsList = new java.util.ArrayList<>(selectedInterests);

        FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .child("interests")
                .setValue(interestsList) // <-- save as list of strings
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