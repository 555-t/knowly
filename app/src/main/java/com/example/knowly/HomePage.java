package com.example.knowly;

import android.content.Intent; // Required for navigating
import android.os.Bundle;
import android.view.View; // Required for the click listener
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView; // Required for the button type

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        // 1. Find the button by the ID you set in the XML
        MaterialCardView createPostBtn = findViewById(R.id.createpostbutton);

        // 2. Set the click listener
        createPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3. Create an Intent to go from HomePage to CreatePostActivity
                Intent intent = new Intent(HomePage.this, CreatePostActivity.class);
                startActivity(intent);
            }
        });
    }
}