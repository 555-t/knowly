package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        // Connect the button to the code
        MaterialCardView createPostBtn = findViewById(R.id.createpostbutton);

        createPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This starts the transition to the new page
                Intent intent = new Intent(HomePage.this, CreatePostActivity.class);
                startActivity(intent);
            }
        });
    }
}