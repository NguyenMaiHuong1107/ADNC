package com.example.qldb.ActiVity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.qldb.Adapter.AdminSectionsPagerAdapter;
import com.example.qldb.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AdminReservationsFragment extends Fragment {

    // File này không cần onResume hay loadReservations

    private AdminSectionsPagerAdapter adminSectionsPagerAdapter;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout mới có TabLayout và ViewPager2
        return inflater.inflate(R.layout.fragment_admin_reservations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adminSectionsPagerAdapter = new AdminSectionsPagerAdapter(this);
        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);

        viewPager.setAdapter(adminSectionsPagerAdapter);

        // Liên kết Tab và ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Đơn chờ xác nhận");
                    } else {
                        tab.setText("Đơn đã xử lý");
                    }
                }
        ).attach();
    }
}