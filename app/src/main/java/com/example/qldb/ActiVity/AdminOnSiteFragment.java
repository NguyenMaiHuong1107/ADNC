package com.example.qldb.ActiVity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.viewpager2.widget.ViewPager2; // ⭐️ THÊM IMPORT NÀY

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.qldb.DatabaseHelper;
import com.example.qldb.R;
import com.google.android.material.textfield.TextInputEditText;

public class AdminOnSiteFragment extends Fragment {

    private TextInputEditText etOnSiteName, etOnSitePhone, etOnSiteGuests;
    private Button btnCreateOnSite;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_on_site, container, false);

        dbHelper = new DatabaseHelper(getContext());
        etOnSiteName = view.findViewById(R.id.etOnSiteName);
        etOnSitePhone = view.findViewById(R.id.etOnSitePhone);
        etOnSiteGuests = view.findViewById(R.id.etOnSiteGuests);
        btnCreateOnSite = view.findViewById(R.id.btnCreateOnSite);

        btnCreateOnSite.setOnClickListener(v -> createOnSiteBooking());

        return view;
    }

    private void createOnSiteBooking() {
        String name = etOnSiteName.getText().toString().trim();
        String phone = etOnSitePhone.getText().toString().trim();
        String guestsStr = etOnSiteGuests.getText().toString().trim();

        // --- 1. Xác thực dữ liệu ---
        if (name.isEmpty() || phone.isEmpty() || guestsStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int numGuests;
        try {
            numGuests = Integer.parseInt(guestsStr);
            if (numGuests <= 0) {
                Toast.makeText(getContext(), "Số lượng khách phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số lượng khách không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        int adminUserId = getAdminUserId();
        if (adminUserId == -1) {
            Toast.makeText(getContext(), "Lỗi session Admin", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- 2. Gọi DatabaseHelper ---
        boolean success = dbHelper.createOnSiteBooking(name, phone, numGuests, adminUserId);

        // --- 3. Xử lý kết quả ---
        if (success) {
            Toast.makeText(getContext(), "Thêm khách thành công! Đã gán bàn.", Toast.LENGTH_LONG).show();
            // Xóa trống các ô
            etOnSiteName.setText("");
            etOnSitePhone.setText("");
            etOnSiteGuests.setText("");
            etOnSiteName.requestFocus();

            // ⭐️ THÊM 2 DÒNG NÀY ĐỂ TỰ ĐỘNG CHUYỂN TAB
            if (getParentFragment() != null && getParentFragment().getView() != null) {
                ViewPager2 viewPager = getParentFragment().getView().findViewById(R.id.view_pager);
                viewPager.setCurrentItem(1); // Chuyển về tab 1 (Đơn đã xử lý)
            }

        } else {
            Toast.makeText(getContext(), "Thêm thất bại! Đã hết bàn trống.", Toast.LENGTH_LONG).show();
        }
    }

    private int getAdminUserId() {
        if (getActivity() == null) return -1;
        SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }
}

