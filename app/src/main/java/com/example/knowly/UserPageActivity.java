package com.example.knowly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class UserPageActivity extends AppCompatActivity {

    private MaterialCardView btnMenuContainer;
    private CardView logoutMenu;

    private TextView menuLogout, menuDelete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "UserPageActivity LOADED", Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_userpage);

        // bottom nav
        NavigationHelper.setupNavigation(this);

        btnMenuContainer = findViewById(R.id.btnMenuContainer);
        logoutMenu = findViewById(R.id.logoutMenu);

        // menu items from included menu_setting.xml
        menuLogout = findViewById(R.id.menu_logout);
        menuDelete = findViewById(R.id.menu_delete);

        // force menu button above other views
        btnMenuContainer.bringToFront();

        btnMenuContainer.setOnClickListener(v -> {
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
            toggleMenu();
        });

        // click outside to close (tap anywhere)
        View root = findViewById(android.R.id.content);
        root.setOnClickListener(v -> logoutMenu.setVisibility(View.GONE));

        menuLogout.setOnClickListener(v -> {
            logoutMenu.setVisibility(View.GONE);

            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(UserPageActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        menuDelete.setOnClickListener(v -> {
            logoutMenu.setVisibility(View.GONE);
            Toast.makeText(this, "Delete Account clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void toggleMenu() {
        if (logoutMenu.getVisibility() == View.VISIBLE) {
            logoutMenu.setVisibility(View.GONE);
        } else {
            logoutMenu.setVisibility(View.VISIBLE);
            logoutMenu.bringToFront();
        }
    }
}



