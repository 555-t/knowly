package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity{

    private static final int SPLASH_DELAY = 1500; // 1.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use the layout you created in Design Mode
        setContentView(R.layout.activity_splash);

        // Wait for SPLASH_DELAY then start LoginActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class)); //check the login activity w sya
            finish(); // close SplashActivity so user can't go back to it
        }, SPLASH_DELAY);
    }
}
