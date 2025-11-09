package com.example.qldb.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.qldb.BangGiaFragment;
import com.example.qldb.DeXuatFragment;
import com.example.qldb.QuyDinhFragment;
import com.example.qldb.TomTatFragment;

public class RestaurantPagerAdapter extends FragmentStateAdapter {

    public RestaurantPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DeXuatFragment();
            case 1:
                return new TomTatFragment();
            case 2:
                return new BangGiaFragment();
            case 3:
                return new QuyDinhFragment();
            default:
                return new DeXuatFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
