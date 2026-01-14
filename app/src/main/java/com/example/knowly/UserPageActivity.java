package com.example.knowly;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class UserPageActivity extends AppCompatActivity {

    private MaterialCardView btnMenuContainer;
    private View btnMenuIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userpage);

        NavigationHelper.setupNavigation(this);

        btnMenuContainer = findViewById(R.id.btnMenuContainer);
        btnMenuIcon = findViewById(R.id.btnMenuIcon);

        View.OnClickListener menuClick = v -> {
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
            showSettingsPopup(btnMenuContainer); // anchor to the card
        };

        btnMenuContainer.setOnClickListener(menuClick);
        btnMenuIcon.setOnClickListener(menuClick);
    }

    private void showSettingsPopup(View anchor) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.menu_setting, null);

        TextView logout = popupView.findViewById(R.id.menu_logout);
        TextView delete = popupView.findViewById(R.id.menu_delete);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setElevation(12f);

        View root = findViewById(android.R.id.content);
        int[] loc = new int[2];
        anchor.getLocationOnScreen(loc);

        popupWindow.showAtLocation(
                root,
                Gravity.TOP | Gravity.END,
                30,
                loc[1] + anchor.getHeight() + 20
        );

        logout.setOnClickListener(v -> {
            popupWindow.dismiss();
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(UserPageActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        delete.setOnClickListener(v -> {
            popupWindow.dismiss();
            Toast.makeText(UserPageActivity.this, "Delete Account clicked", Toast.LENGTH_SHORT).show();
        });
    }
}

