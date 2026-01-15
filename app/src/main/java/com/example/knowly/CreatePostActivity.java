package com.example.knowly;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatePostActivity extends BaseActivity {

    private static final int PICK_IMAGE_CODE = 101;
    private Uri imageUri = null;

    private EditText postContent;
    private AppCompatButton btnPost;
    private Button btnAddImage;
    private ImageButton backButton;

    private FirebaseFirestore db;
    private ChipGroup chipGroupCategories;
    private TextView tvUsername;

    private List<String> selectedCategoriesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createpost_activity);

        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        postContent = findViewById(R.id.post_content);
        btnPost = findViewById(R.id.btnPost);
        btnAddImage = findViewById(R.id.add_image);
        backButton = findViewById(R.id.backButton);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        tvUsername = findViewById(R.id.username); // Change ID to match your XML
        backButton.setOnClickListener(v -> finish());

        loadUserData(); // Add this call

        btnPost.setOnClickListener(v -> {
            String text = postContent.getText().toString().trim();
            if (!text.isEmpty()) {
                uploadPostToFirestore(text, selectedCategoriesList);
            } else {
                Toast.makeText(this, "Please write something...", Toast.LENGTH_SHORT).show();
            }
        });

        loadCategoriesFromFirestore(); // Fetches global category list
    }

    private void uploadPostToFirestore(String content, List<String> selectedCategories) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("author", userId); // Stores UID for relational lookup
        postMap.put("content", content);
        postMap.put("categories", selectedCategories);
        postMap.put("upvote_num", 0);
        postMap.put("downvote_num", 0);
        postMap.put("comment_num", 0);
        postMap.put("timestamp", FieldValue.serverTimestamp()); // Uses server-side time

        db.collection("posts").add(postMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Post Created!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void createCategoryChip(String categoryName) {
        Chip chip = new Chip(this);
        chip.setText(categoryName);
        chip.setCheckable(true);
        chip.setBackgroundResource(R.drawable.chip_cat_background_selector);
        chip.setTextColor(getResources().getColorStateList(R.color.chip_cat_text_selector));

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (selectedCategoriesList.size() < 3) {
                    selectedCategoriesList.add(categoryName);
                } else {
                    chip.setChecked(false);
                    Toast.makeText(this, "Maximum 3 categories allowed", Toast.LENGTH_SHORT).show();
                }
            } else {
                selectedCategoriesList.remove(categoryName);
            }
        });
        chipGroupCategories.addView(chip);
    }

    private void loadCategoriesFromFirestore() {
        db.collection("categories").document("allCategories").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> categories = (List<String>) documentSnapshot.get("list");
                        if (categories != null) {
                            chipGroupCategories.removeAllViews();
                            for (String cat : categories) {
                                createCategoryChip(cat);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error: " + e.getMessage()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Toast.makeText(this, "Image Attached!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String actualName = documentSnapshot.getString("username");
                if (actualName != null && !actualName.isEmpty()) {
                    tvUsername.setText(actualName);
                }
            }
        });
    }
}