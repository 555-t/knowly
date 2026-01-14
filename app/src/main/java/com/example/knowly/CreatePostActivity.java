package com.example.knowly;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class CreatePostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This links the Java code to your createpost_activity.xml
        setContentView(R.layout.createpost_activity);
    }
}