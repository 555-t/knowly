package com.example.knowly;



import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;



public class SearchActivity extends AppCompatActivity {

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

// You can reuse your existing search layout if you have one

        setContentView(R.layout.fragment_search);

        NavigationHelper.setupNavigation(this);

    }

}