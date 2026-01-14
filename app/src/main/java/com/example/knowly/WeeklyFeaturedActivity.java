package com.example.knowly;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class WeeklyFeaturedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure this matches the filename of your Weekly Featured XML
        setContentView(R.layout.fragment_weeklyfeatured);
    }
}