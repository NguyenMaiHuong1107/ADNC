package com.example.qldb.ActiVity;

import android.content.Context; // ⭐️ THÊM IMPORT
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.qldb.DatabaseHelper;
import com.example.qldb.R;
import com.example.qldb.ReservationStatus;

public class AdminHomeFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView tvAvailableTablesCount, tvPendingCount, tvConfirmedCount;
    private TextView tvOnSiteCount;
    private Button btnEndDay;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dbHelper = new DatabaseHelper(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        // Ánh xạ View
        tvAvailableTablesCount = view.findViewById(R.id.tvAvailableTablesCount);
        tvPendingCount = view.findViewById(R.id.tvPendingCount);
        tvConfirmedCount = view.findViewById(R.id.tvConfirmedCount);
        tvOnSiteCount = view.findViewById(R.id.tvOnSiteCount);
        btnEndDay = view.findViewById(R.id.btnEndDay);

        btnEndDay.setOnClickListener(v -> {
            showEndDayConfirmationDialog();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        if (!isAdded() || dbHelper == null) {
            return;
        }

        // Bỏ kiểm tra 'if (dbHelper == null)' vì nó đã được khởi tạo trong onAttach
        int availableTables = dbHelper.getCountOfTablesByStatus("available");
        int pendingReservations = dbHelper.getCountOfReservationsByStatus(ReservationStatus.PENDING.getValue());
        int confirmedReservations = dbHelper.getCountOfReservationsByStatus(ReservationStatus.CONFIRMED.getValue());
        int onSiteReservations = dbHelper.getCountOfReservationsByStatus(ReservationStatus.ON_SITE.getValue());

        // Các kiểm tra 'if (tv... != null)' này vẫn tốt để giữ lại
        if (tvAvailableTablesCount != null) {
            tvAvailableTablesCount.setText("Số lượng: " + availableTables);
        }
        if (tvPendingCount != null) {
            tvPendingCount.setText("Số lượng: " + pendingReservations);
        }
        if (tvConfirmedCount != null) {
            tvConfirmedCount.setText("Số lượng: " + confirmedReservations);
        }
        if (tvOnSiteCount != null) {
            tvOnSiteCount.setText("Số lượng: " + onSiteReservations);
        }
    }

    private void showEndDayConfirmationDialog() {
        if (getContext() == null || !isAdded()) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận Kết thúc ngày")
                .setMessage("Bạn có chắc chắn muốn reset tất cả đơn hàng về 0 và đặt lại số bàn trống là 30 không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    performEndDayReset();
                })
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performEndDayReset() {
        if (!isAdded() || dbHelper == null) {
            return;
        }

        dbHelper.clearAllReservations();
        dbHelper.resetAllTablesToAvailable();

        loadDashboardData();
        if (getContext() != null) {
            Toast.makeText(getContext(), "Đã reset. Bắt đầu ngày mới!", Toast.LENGTH_SHORT).show();
        }
    }
}