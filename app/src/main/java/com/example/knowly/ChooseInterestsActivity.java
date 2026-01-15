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
// REMOVED: import com.google.firebase.database.FirebaseDatabase;
// ADDED: Firestore
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChooseInterestsActivity extends AppCompatActivity {

    private final Set<String> selectedInterests = new HashSet<>();
    private FirebaseFirestore db; // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_interest);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

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
        // Safety check to avoid crashes if layout is slightly different
        if (card.getChildCount() > 0 && card.getChildAt(0) instanceof ViewGroup) {
            ViewGroup bg = (ViewGroup) card.getChildAt(0);
            TextView text = (TextView) bg.getChildAt(0);

            // Find check icon by tag or ID if you have it
            // Assuming you set android:tag="checkIcon" in XML
            ImageView checkIcon = card.findViewWithTag("checkIcon");

            // If checkIcon is null, try finding by ID if known, otherwise skip visual toggle for icon
            // ImageView checkIcon = card.findViewById(R.id.your_check_icon_id);

            card.setOnClickListener(v -> {
                boolean checked = !card.isChecked();
                card.setChecked(checked);

                bg.setSelected(checked);
                text.setSelected(checked);

                if (checkIcon != null) {
                    checkIcon.setVisibility(checked ? View.VISIBLE : View.GONE);
                }

                card.setStrokeColor(
                        checked
                                ? ContextCompat.getColor(this, android.R.color.white)
                                : ContextCompat.getColor(this, R.color.dark_gray)
                );

                if (checked) {
                    selectedInterests.add(interest);
                } else {
                    selectedInterests.remove(interest);
                }
            });
        }
    }

    private void saveInterestsAndProceed() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        List<String> interestsList = new ArrayList<>(selectedInterests);

        // --- THE FIX: SAVE TO FIRESTORE ---

        // We use a Map to update just one field
        Map<String, Object> data = new HashMap<>();
        data.put("interests", interestsList);

        // Update the existing document in "users" collection
        db.collection("users").document(uid)
                .update(data) // .update fails if doc doesn't exist, .set(..., SetOptions.merge()) creates it if missing
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Interests saved!", Toast.LENGTH_SHORT).show();

                    // Go to Home Page
                    Intent intent = new Intent(this, HomePage.class);
                    // Clear history so they can't go back to selection screen
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Fallback: If document didn't exist for some reason, create it
                    db.collection("users").document(uid)
                            .set(data, SetOptions.merge())
                            .addOnSuccessListener(u -> {
                                startActivity(new Intent(this, HomePage.class));
                                finish();
                            });
                });
    }
}