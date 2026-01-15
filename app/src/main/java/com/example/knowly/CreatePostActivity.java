package com.example.knowly;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreatePostActivity extends BaseActivity {

    private static final int PICK_MEDIA_CODE = 101;
    private Uri mediaUri = null;
    private String mediaType = "none"; // "image" or "video" or "none"

    private EditText postContent;
    private AppCompatButton btnPost;
    private Button btnAddMedia;
    private ImageButton backButton, btnRemoveMedia;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private ChipGroup chipGroupCategories;

    // Preview Views
    private FrameLayout mediaPreviewContainer;
    private ImageView imagePreview;
    private VideoView videoPreview;

    private List<String> selectedCategoriesList = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createpost_activity); // Make sure XML name matches

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Initialize Views
        postContent = findViewById(R.id.post_content);
        btnPost = findViewById(R.id.btnPost);
        btnAddMedia = findViewById(R.id.add_media); // Changed ID in XML
        backButton = findViewById(R.id.backButton);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);

        // Media Preview Views
        mediaPreviewContainer = findViewById(R.id.media_preview_container);
        imagePreview = findViewById(R.id.image_preview);
        videoPreview = findViewById(R.id.video_preview);
        btnRemoveMedia = findViewById(R.id.btn_remove_media);

        progressDialog = new ProgressDialog(this);

        backButton.setOnClickListener(v -> finish());

        // 1. OPEN GALLERY (Images OR Videos)
        btnAddMedia.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});
            startActivityForResult(intent, PICK_MEDIA_CODE);
        });

        // 2. REMOVE MEDIA
        btnRemoveMedia.setOnClickListener(v -> {
            mediaUri = null;
            mediaType = "none";
            mediaPreviewContainer.setVisibility(View.GONE);
            videoPreview.stopPlayback();
        });

        // 3. POST BUTTON
        btnPost.setOnClickListener(v -> {
            String text = postContent.getText().toString().trim();
            if (!text.isEmpty()) {
                if (mediaUri != null) {
                    uploadMediaAndPost(text);
                } else {
                    savePostToDatabase(text, "none", "none");
                }
            } else {
                Toast.makeText(this, "Please write something...", Toast.LENGTH_SHORT).show();
            }
        });

        loadCategoriesFromFirestore();
    }

    // --- UPLOAD LOGIC (Firebase Storage) ---
    private void uploadMediaAndPost(String content) {
        if (selectedCategoriesList.isEmpty()) {
            Toast.makeText(this, "Please select at least 1 category", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Uploading " + mediaType + "...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Create a unique filename based on time
        String folder = mediaType.equals("video") ? "Post_Videos" : "Post_Images";
        String fileName = System.currentTimeMillis() + "." + getFileExtension(mediaUri);
        StorageReference fileRef = mStorageRef.child(folder).child(fileName);

        fileRef.putFile(mediaUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        savePostToDatabase(content, downloadUrl, mediaType);
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void savePostToDatabase(String content, String url, String type) {
        if (selectedCategoriesList.isEmpty()) {
            Toast.makeText(this, "Please select at least 1 category", Toast.LENGTH_SHORT).show();
            if(progressDialog.isShowing()) progressDialog.dismiss();
            return;
        }

        String currentUid = FirebaseAuth.getInstance().getUid();
        String postId = mDatabase.push().getKey();

        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("postId", postId);
        postMap.put("content", content);
        postMap.put("author", currentUid);
        postMap.put("timestamp", ServerValue.TIMESTAMP);

        // --- NEW FIELDS ---
        postMap.put("mediaUrl", url);   // The Storage Link
        postMap.put("mediaType", type); // "image" or "video"
        postMap.put("categories", selectedCategoriesList);

        postMap.put("upvote_num", 0);
        postMap.put("downvote_num", 0);
        postMap.put("comment_num", 0);

        if (postId != null) {
            btnPost.setEnabled(false);
            mDatabase.child(postId).setValue(postMap).addOnSuccessListener(aVoid -> {
                if(progressDialog.isShowing()) progressDialog.dismiss();
                Toast.makeText(CreatePostActivity.this, "Posted Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                btnPost.setEnabled(true);
                if(progressDialog.isShowing()) progressDialog.dismiss();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    // --- HANDLE FILE SELECTION ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_MEDIA_CODE && resultCode == RESULT_OK && data != null) {
            mediaUri = data.getData();

            // Check MIME Type
            ContentResolver cr = getContentResolver();
            String mime = cr.getType(mediaUri);

            mediaPreviewContainer.setVisibility(View.VISIBLE);

            if (mime != null && mime.startsWith("video")) {
                // IT IS A VIDEO
                mediaType = "video";
                imagePreview.setVisibility(View.GONE);
                videoPreview.setVisibility(View.VISIBLE);

                videoPreview.setVideoURI(mediaUri);
                // Add controls to play/pause the preview
                MediaController mediaController = new MediaController(this);
                videoPreview.setMediaController(mediaController);
                mediaController.setAnchorView(videoPreview);
                videoPreview.start(); // Auto play preview

            } else {
                // IT IS AN IMAGE
                mediaType = "image";
                videoPreview.setVisibility(View.GONE);
                imagePreview.setVisibility(View.VISIBLE);
                imagePreview.setImageURI(mediaUri);
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    // --- CATEGORIES LOGIC (Same as before) ---
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
}
