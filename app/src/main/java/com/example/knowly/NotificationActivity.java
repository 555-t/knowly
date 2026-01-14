package com.example.knowly; // Ensure this matches your package name

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If you don't have a specific notifications XML yet,
        // you can temporarily use home_page just to test if the click works.
        setContentView(R.layout.notifications_activity);

        // IMPORTANT: Call the helper so the nav bar works here too!
        NavigationHelper.setupNavigation(this);
    }
}