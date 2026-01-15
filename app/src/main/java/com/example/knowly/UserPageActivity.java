package com.example.knowly;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserPageActivity extends AppCompatActivity {

    private MaterialCardView btnMenuContainer;
    private CardView logoutMenu, btnEditProfile;
    private TextView tvName, tvEmail, tvAvatarText, tvBio, tvCredentials;
    private TextView tvPostsCount, tvFollowersCount, tvFollowingCount;
    private TextView menuLogout, menuDelete;
    private ChipGroup cgInterests;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userpage);

        // 1. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 2. Initialize Views
        btnMenuContainer = findViewById(R.id.btnMenuContainer);
        logoutMenu = findViewById(R.id.logoutMenu);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvAvatarText = findViewById(R.id.tvAvatarText);
        tvBio = findViewById(R.id.tvBio);
        tvCredentials = findViewById(R.id.tvCredentials);

        tvPostsCount = findViewById(R.id.tvPostsCount);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);

        cgInterests = findViewById(R.id.cgInterests);
        menuLogout = findViewById(R.id.menu_logout);
        menuDelete = findViewById(R.id.menu_delete);

        // 3. Setup TabLayout & ViewPager2
        TabLayout tabLayout = findViewById(R.id.profileTabs);
        ViewPager2 viewPager = findViewById(R.id.profileViewPager);

        ProfilePagerAdapter pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("My Posts"); break;
                case 1: tab.setText("Bookmarks"); break;
                case 2: tab.setText("Streaks"); break;
            }
        }).attach();

        // 4. Navigation & Menu Setup
        NavigationHelper.setupNavigation(this);

        // 5. Click Listeners
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        btnMenuContainer.setOnClickListener(v -> toggleMenu());

        menuLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        menuDelete.setOnClickListener(v -> {
            Toast.makeText(this, "Account deletion requires re-authentication.", Toast.LENGTH_SHORT).show();
        });

        // 6. Load Initial Data
        loadUserProfile();
    }

    private void loadUserProfile() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        // 1. Fetch User Data & Follow Stats
        mDatabase.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !isFinishing()) {
                    String username = snapshot.child("username").getValue(String.class);
                    tvName.setText(username != null ? username : "User");
                    tvEmail.setText(snapshot.child("email").getValue(String.class));

                    // FOLLOWERS/FOLLOWING:
                    // Counts the number of entries under the "followers" and "following" nodes
                    long followers = snapshot.child("followers").getChildrenCount();
                    long following = snapshot.child("following").getChildrenCount();

                    tvFollowersCount.setText(followers + " Followers");
                    tvFollowingCount.setText(following + " Following");

                    if (username != null && !username.isEmpty()) {
                        tvAvatarText.setText(username.substring(0, 1).toUpperCase());
                    }
                    displayInterests(snapshot.child("interests"));

                    // 2. Fetch Post Count (Triggered here so we have the 'username' to match)
                    updatePostCount(uid, username);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updatePostCount(String currentUid, String currentUsername) {
        mDatabase.child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot postSnap : snapshot.getChildren()) {
                    // Your DB uses 'author' for username and 'publisher' or 'uid' for IDs
                    String authorField = postSnap.child("author").getValue(String.class);
                    String publisherField = postSnap.child("publisher").getValue(String.class);

                    // Match by either Username OR UID to be safe
                    if (currentUid.equals(publisherField) ||
                            (currentUsername != null && currentUsername.equals(authorField))) {
                        count++;
                    }
                }
                tvPostsCount.setText(count + " Posts");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayInterests(DataSnapshot interestsSnapshot) {
        cgInterests.removeAllViews();
        if (!interestsSnapshot.exists()) return;

        for (DataSnapshot ds : interestsSnapshot.getChildren()) {
            String interest = ds.getValue(String.class);
            if (interest != null) {
                Chip chip = new Chip(this);
                chip.setText(interest);
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
                chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
                chip.setChipStrokeWidth(2f);
                chip.setTextColor(Color.parseColor("#424242"));

                try {
                    chip.setTypeface(ResourcesCompat.getFont(this, R.font.inter_medium));
                } catch (Exception e) {
                    // Fallback to system medium
                    chip.setTypeface(null, android.graphics.Typeface.BOLD);
                }

                chip.setClickable(false);
                chip.setCheckable(false);
                cgInterests.addView(chip);
            }
        }
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