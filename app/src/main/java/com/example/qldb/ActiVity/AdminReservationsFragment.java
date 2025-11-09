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

    private AdminSectionsPagerAdapter adminSectionsPagerAdapter;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_reservations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adminSectionsPagerAdapter = new AdminSectionsPagerAdapter(this);
        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);

        viewPager.setAdapter(adminSectionsPagerAdapter);

        // ⭐️ SỬA LẠI LOGIC NÀY ĐỂ HIỂN THỊ 3 TAB
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Đơn chờ xác nhận");
                            break;
                        case 1:
                            tab.setText("Đơn đã xử lý");
                            break;
                        case 2:
                            tab.setText("Ăn tại quán"); // ⭐️ TAB MỚI
                            break;
                    }
                }
        ).attach();
    }
}