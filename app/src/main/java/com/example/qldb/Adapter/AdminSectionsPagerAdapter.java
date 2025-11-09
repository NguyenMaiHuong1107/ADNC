package com.example.qldb.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.qldb.ActiVity.AdminOnSiteFragment; // ⭐️ IMPORT MỚI
import com.example.qldb.ActiVity.ReservationListFragment;
import com.example.qldb.ReservationStatus;

import java.util.ArrayList;
import java.util.Arrays;

public class AdminSectionsPagerAdapter extends FragmentStateAdapter {

    public AdminSectionsPagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // Tab "Đơn chờ xác nhận"
                ArrayList<String> pendingStatus = new ArrayList<>();
                pendingStatus.add(ReservationStatus.PENDING.getValue());
                return ReservationListFragment.newInstance(pendingStatus);
            case 1:
                // Tab "Đơn đã xử lý"
                // Chỉ hiển thị các đơn đang "active" (Đã xác nhận, Tại quán)
                // Các đơn COMPLETED hoặc CANCELLED sẽ tự biến mất
                ArrayList<String> processedStatuses = new ArrayList<>(
                        Arrays.asList(
                                ReservationStatus.CONFIRMED.getValue(),
                                ReservationStatus.ON_SITE.getValue()
                        )
                );
                return ReservationListFragment.newInstance(processedStatuses);
            case 2:
                // ⭐️ TAB MỚI: "Ăn tại quán" (Form để nhập)
                return new AdminOnSiteFragment();
            default:
                // Trả về fragment "chờ" làm mặc định an toàn
                ArrayList<String> defaultStatus = new ArrayList<>();
                defaultStatus.add(ReservationStatus.PENDING.getValue());
                return ReservationListFragment.newInstance(defaultStatus);
        }
    }

    @Override
    public int getItemCount() {
        // ⭐️ SỬA: Trả về 3 (vì có 3 tab)
        return 3;
    }
}