package com.example.qldb.ActiVity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.qldb.DatabaseHelper;
import com.example.qldb.R;
import com.example.qldb.ReservationStatus;

public class AdminHomeFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView tvAvailableTablesCount, tvPendingCount, tvConfirmedCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        // Khởi tạo dbHelper ở đây
        dbHelper = new DatabaseHelper(getContext());

        // Chỉ ánh xạ View
        tvAvailableTablesCount = view.findViewById(R.id.tvAvailableTablesCount);
        tvPendingCount = view.findViewById(R.id.tvPendingCount);
        tvConfirmedCount = view.findViewById(R.id.tvConfirmedCount);

        // ⭐️ KHÔNG load data ở đây nữa, vì onResume sẽ chạy ngay sau
        // loadDashboardData();

        return view;
    }

    // ⭐️ THÊM HÀM NÀY
    @Override
    public void onResume() {
        super.onResume();

        // Tải (hoặc tải lại) dữ liệu mỗi khi Fragment này được hiển thị
        // Điều này đảm bảo số liệu trên dashboard luôn chính xác
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Kiểm tra dbHelper, vì getContext() có thể null nếu Fragment chưa attach
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(getContext());
        }

        int availableTables = dbHelper.getCountOfTablesByStatus("available");
        int pendingReservations = dbHelper.getCountOfReservationsByStatus(ReservationStatus.PENDING.getValue());
        int confirmedReservations = dbHelper.getCountOfReservationsByStatus(ReservationStatus.CONFIRMED.getValue());

        // Đảm bảo các View không null trước khi set (an toàn hơn)
        if (tvAvailableTablesCount != null) {
            tvAvailableTablesCount.setText("Số lượng: " + availableTables);
        }
        if (tvPendingCount != null) {
            tvPendingCount.setText("Số lượng: " + pendingReservations);
        }
        if (tvConfirmedCount != null) {
            tvConfirmedCount.setText("Số lượng: " + confirmedReservations);
        }
    }
}