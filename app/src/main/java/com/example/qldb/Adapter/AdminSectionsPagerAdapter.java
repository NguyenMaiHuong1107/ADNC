package com.example.qldb.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

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
        // position 0: Tab "Đơn chờ xác nhận"
        if (position == 0) {
            // Chỉ lấy đơn PENDING
            ArrayList<String> pendingStatus = new ArrayList<>(
                    Arrays.asList(ReservationStatus.PENDING.getValue())
            );
            return ReservationListFragment.newInstance(pendingStatus);
        }

        // position 1: Tab "Đơn đã xác nhận"
        // Lấy đơn CONFIRMED và COMPLETED
        ArrayList<String> confirmedStatuses = new ArrayList<>(
                Arrays.asList(
                        ReservationStatus.CONFIRMED.getValue(),
                        ReservationStatus.COMPLETED.getValue(),
                        ReservationStatus.CANCELLED.getValue() // Bạn cũng có thể muốn xem đơn đã hủy
                )
        );
        return ReservationListFragment.newInstance(confirmedStatuses);
    }

    @Override
    public int getItemCount() {
        return 2; // Chúng ta có 2 tab
    }
}