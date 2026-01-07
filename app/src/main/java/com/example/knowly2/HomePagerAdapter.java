package com.example.knowly2;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomePagerAdapter extends FragmentStateAdapter {
    public HomePagerAdapter(@NonNull FragmentActivity fa) { super(fa); }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) return FeedFragment.newInstance("following");
        return FeedFragment.newInstance("for_you");
    }

    @Override
    public int getItemCount() { return 2; }
}
