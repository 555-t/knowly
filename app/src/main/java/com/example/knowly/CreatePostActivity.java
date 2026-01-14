package com.example.knowly;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreatePostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_CODE = 101;
    private Uri imageUri = null;

    private EditText postContent;
    private AppCompatButton btnPost;
    private Button btnAddImage;
    private ImageButton backButton;
    private DatabaseReference mDatabase;
    private ChipGroup chipGroupCategories;

    // This list tracks your 1-3 selected categories
    private List<String> selectedCategoriesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createpost_activity);

        // 1. Initialize Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");

        // 2. Initialize Views
        postContent = findViewById(R.id.post_content);
        btnPost = findViewById(R.id.btnPost);
        btnAddImage = findViewById(R.id.add_image);
        backButton = findViewById(R.id.backButton);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);

        backButton.setOnClickListener(v -> finish());

        btnAddImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_CODE);
        });

        // 3. Post Button Click Listener
        btnPost.setOnClickListener(v -> {
            String text = postContent.getText().toString().trim();
            if (!text.isEmpty()) {
                uploadPost(text);
            } else {
                Toast.makeText(this, "Please write something...", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Load chips from Firestore
        loadCategoriesFromFirestore();
    }

    private void createCategoryChip(String categoryName) {
        Chip chip = new Chip(this);
        chip.setText(categoryName);
        chip.setCheckable(true);

        // Set the background and text color selectors
        chip.setBackgroundResource(R.drawable.chip_cat_background_selector);
        chip.setTextColor(getResources().getColorStateList(R.color.chip_cat_text_selector));

        // Logic for selecting Max 3
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (selectedCategoriesList.size() < 3) {
                    selectedCategoriesList.add(categoryName);
                } else {
                    chip.setChecked(false); // Undo selection if already at limit
                    Toast.makeText(this, "Maximum 3 categories allowed", Toast.LENGTH_SHORT).show();
                }
            } else {
                selectedCategoriesList.remove(categoryName);
            }
        });

        chipGroupCategories.addView(chip);
    }

    private void uploadPost(String content) {
        // Validation: At least 1 category must be picked
        if (selectedCategoriesList.isEmpty()) {
            Toast.makeText(this, "Please select at least 1 category", Toast.LENGTH_SHORT).show();
            return;
        }

        String postId = mDatabase.push().getKey();
        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("postId", postId);
        postMap.put("content", content);
        postMap.put("author", "student_user");
        postMap.put("timestamp", System.currentTimeMillis());
        postMap.put("image", imageUri != null ? imageUri.toString() : "none");

        // This saves the list of categories into Firebase
        postMap.put("categories", selectedCategoriesList);

        if (postId != null) {
            btnPost.setEnabled(false); // Prevent multiple clicks
            mDatabase.child(postId).setValue(postMap).addOnSuccessListener(aVoid -> {
                Toast.makeText(CreatePostActivity.this, "Posted Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                btnPost.setEnabled(true);
                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

        postMap.put("postId", postId);
        postMap.put("upvote_num", 0);
        postMap.put("downvote_num", 0);
        postMap.put("comment_num", 0);
    }

    private void loadCategoriesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("categories").document("allCategories").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch the list field from your Firestore document
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
}