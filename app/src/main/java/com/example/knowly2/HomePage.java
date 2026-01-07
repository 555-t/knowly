package com.example.knowly2;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class HomePage extends AppCompatActivity {

    private TextView btnForYou, btnFollowing;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        btnForYou = findViewById(R.id.btnForYou);
        btnFollowing = findViewById(R.id.btnFollowing);
        viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new HomePagerAdapter(this));

        // default
        selectTab(0);

        btnForYou.setOnClickListener(v -> selectTab(0));
        btnFollowing.setOnClickListener(v -> selectTab(1));

        // swipe also updates pills
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                selectTab(position);
            }
        });

        // Bottom icon clicks
        ImageView navHome = findViewById(R.id.navHome);
        ImageView navTrend = findViewById(R.id.navTrend);
        ImageView navSearch = findViewById(R.id.navSearch);
        ImageView navNotif = findViewById(R.id.navNotif);

        navHome.setOnClickListener(v -> Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show());
        navTrend.setOnClickListener(v -> Toast.makeText(this, "Trending", Toast.LENGTH_SHORT).show());
        navSearch.setOnClickListener(v -> Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show());
        navNotif.setOnClickListener(v -> Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show());

        findViewById(R.id.navProfile).setOnClickListener(v ->
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
        );
    }

    private void selectTab(int index) {
        viewPager.setCurrentItem(index, true);

        if (index == 0) {
            btnForYou.setBackgroundResource(R.drawable.bg_tab_selected);
            btnForYou.setTextColor(Color.WHITE);

            btnFollowing.setBackgroundColor(Color.TRANSPARENT);
            btnFollowing.setTextColor(Color.parseColor("#7A7A7A"));
        } else {
            btnFollowing.setBackgroundResource(R.drawable.bg_tab_selected);
            btnFollowing.setTextColor(Color.WHITE);

            btnForYou.setBackgroundColor(Color.TRANSPARENT);
            btnForYou.setTextColor(Color.parseColor("#7A7A7A"));
        }
    }
}