package com.example.knowly;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProfilePagerAdapter extends FragmentStateAdapter {

    public ProfilePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // This connects your fragments to the tabs
        switch (position) {
            case 0:
                return new MyPostsFragment();
            case 1:
                return new BookmarksFragment();
            case 2:
                return new StreaksFragment();
            default:
                return new MyPostsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Number of tabs: My Posts, Bookmarks, Streaks
    }
}