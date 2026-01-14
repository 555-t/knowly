package com.example.knowly;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomePage extends AppCompatActivity {

    private CardView cardForYou, cardFollowing;
    private TextView btnForYou, btnFollowing;

    // Start opposite so the first call applies styles
    private boolean isForYouSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        cardForYou = findViewById(R.id.cardForYou);
        cardFollowing = findViewById(R.id.cardFollowing);
        btnForYou = findViewById(R.id.btnForYou);
        btnFollowing = findViewById(R.id.btnFollowing);

        setSelectedTab(true);

        cardForYou.setOnClickListener(v -> setSelectedTab(true));
        cardFollowing.setOnClickListener(v -> setSelectedTab(false));
    }

    private void setSelectedTab(boolean isForYou) {
        if (isForYou == isForYouSelected) return;
        isForYouSelected = isForYou;

        if (isForYouSelected) {
            // FOR YOU selected (gradient on text)
            btnForYou.setBackgroundResource(R.drawable.gradient_button);
            btnForYou.setTextColor(Color.WHITE);

            // FOLLOWING unselected (flat text)
            btnFollowing.setBackgroundResource(R.drawable.bg_tab_flat);
            btnFollowing.setTextColor(Color.parseColor("#7A7A7A"));

            cardForYou.setCardBackgroundColor(Color.TRANSPARENT);
            cardFollowing.setCardBackgroundColor(Color.TRANSPARENT);

        } else {
            // FOLLOWING selected
            btnFollowing.setBackgroundResource(R.drawable.gradient_button);
            btnFollowing.setTextColor(Color.WHITE);

            // FOR YOU unselected
            btnForYou.setBackgroundResource(R.drawable.bg_tab_flat);
            btnForYou.setTextColor(Color.parseColor("#7A7A7A"));

            cardForYou.setCardBackgroundColor(Color.TRANSPARENT);
            cardFollowing.setCardBackgroundColor(Color.TRANSPARENT);
        }
    }
}


