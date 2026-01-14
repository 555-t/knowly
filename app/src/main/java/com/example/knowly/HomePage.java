package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        // 1. Find the navWeekly view inside the included bottom_nav_bar
        // Since it's included, it's part of the main view hierarchy
        ImageView navWeekly = findViewById(R.id.navWeekly);

        // 2. Set the click listener
        navWeekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3. Create an Intent to start the Weekly Featured activity
                Intent intent = new Intent(HomePage.this, WeeklyFeaturedActivity.class);
                startActivity(intent);
            }
        });
    }
}