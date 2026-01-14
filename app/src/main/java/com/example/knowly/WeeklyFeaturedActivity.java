package com.example.knowly;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class WeeklyFeaturedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_weeklyfeatured);

        //navigates for all navi bar buttons
        NavigationHelper.setupNavigation(this);
    }
}