package com.example.knowly;

import androidx.annotation.NonNull;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.Fragment;

public class ViewPagerAdapter extends FragmentStateAdapter{
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity){
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position){
        switch (position){
            case 0: return new MyPostsFragment();
            case 1: return new BookmarksFragment();
            case 2: return new StreaksFragment();
            default: return new MyPostsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Total number of tabs
    }
}



