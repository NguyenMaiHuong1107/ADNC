package com.example.qldb.ActiVity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.qldb.Adapter.AdminReservationAdapter;
import com.example.qldb.DatabaseHelper;
import com.example.qldb.R;
import com.example.qldb.ReservationModel;
import com.example.qldb.ReservationStatus;

import java.util.ArrayList;
import java.util.List;

// ⭐️ Đảm bảo Fragment implements HÀM MỚI trong interface
public class ReservationListFragment extends Fragment implements AdminReservationAdapter.OnActionClickListener {

    private static final String ARG_STATUSES = "statuses";

    private ListView lvReservations;
    private DatabaseHelper dbHelper;
    private AdminReservationAdapter adapter;
    private List<ReservationModel> reservationList;
    private ArrayList<String> statusesToShow;

    public static ReservationListFragment newInstance(ArrayList<String> statuses) {
        ReservationListFragment fragment = new ReservationListFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_STATUSES, statuses);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            statusesToShow = getArguments().getStringArrayList(ARG_STATUSES);
        }
        dbHelper = new DatabaseHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservation_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lvReservations = view.findViewById(R.id.lvReservations);
        // Tải lần đầu (sẽ được gọi lại trong onResume)
        loadReservations();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            loadReservations();
        }
    }

    // ⭐️ SỬA HÀM NÀY
    private void loadReservations() {
        if (statusesToShow == null || statusesToShow.isEmpty()) return;
        if (getContext() == null) return;

        reservationList = dbHelper.getReservationsByStatusList(statusesToShow);

        // ⭐️ XÁC ĐỊNH LOẠI NÚT CẦN HIỂN THỊ
        // 1. Hiện nút Confirm/Cancel (✓/✗) nếu tab này là "Chờ xác nhận"
        boolean showPending = statusesToShow.contains(ReservationStatus.PENDING.getValue());

        // 2. Hiện nút "Ăn xong" nếu tab này là "Đã xử lý" (chứa đơn Confirmed và On-site)
        boolean showFinish = statusesToShow.contains(ReservationStatus.CONFIRMED.getValue())
                || statusesToShow.contains(ReservationStatus.ON_SITE.getValue());

        // ⭐️ GỌI CONSTRUCTOR MỚI CỦA ADAPTER
        adapter = new AdminReservationAdapter(getContext(), reservationList, showPending, showFinish);

        // Luôn set listener (adapter sẽ tự xử lý nút nào được bấm)
        adapter.setOnActionClickListener(this);

        lvReservations.setAdapter(adapter);
    }

    // --- Logic xử lý khi bấm nút ---

    @Override
    public void onConfirmClick(ReservationModel reservation) {
        // (Giữ nguyên code cũ của bạn)
        showConfirmationDialog(reservation, ReservationStatus.CONFIRMED, "Xác nhận đơn này?");
    }

    @Override
    public void onCancelClick(ReservationModel reservation) {
        // (Giữ nguyên code cũ của bạn)
        showDeleteDialog(reservation);
    }

    // ⭐️ THÊM HÀM MỚI NÀY (XỬ LÝ NÚT "ĂN XONG")
    @Override
    public void onFinishClick(ReservationModel reservation) {
        // Hiển thị dialog xác nhận "Khách đã ăn xong"
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Hoàn thành đơn")
                .setMessage("Xác nhận khách tại đơn " + reservation.id + " đã ăn xong và trả bàn?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    finishDining(reservation); // Gọi hàm helper để xử lý
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ⭐️ THÊM HÀM HELPER NÀY
    private void finishDining(ReservationModel reservation) {
        try {
            // Gọi hàm mới trong DatabaseHelper
            dbHelper.completeReservation(reservation.id);
            Toast.makeText(getContext(), "Đã hoàn thành đơn " + reservation.id, Toast.LENGTH_SHORT).show();

            // Tải lại danh sách (đơn này sẽ biến mất khỏi tab "Đã xử lý")
            loadReservations();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khi hoàn thành đơn", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // (Hàm showDeleteDialog và deleteReservation của bạn giữ nguyên)
    private void showDeleteDialog(ReservationModel reservation) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa đơn: ".concat(String.valueOf(reservation.id)))
                .setMessage("Bạn có chắc muốn XÓA vĩnh viễn đơn này?\nKhách: " + reservation.getContactInfo())
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteReservation(reservation);
                })
                .setNegativeButton("Không", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteReservation(ReservationModel reservation) {
        try {
            dbHelper.deleteReservation(reservation.id);
            Toast.makeText(getContext(), "Đã xóa đơn " + reservation.id, Toast.LENGTH_SHORT).show();
            loadReservations();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    // (Hàm showConfirmationDialog của bạn giữ nguyên)
    private void showConfirmationDialog(ReservationModel reservation, ReservationStatus newStatus, String message) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xử lý đơn: ".concat(String.valueOf(reservation.id)))
                .setMessage(message + "\nKhách: " + reservation.getContactInfo())
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    updateStatus(reservation, newStatus);
                })
                .setNegativeButton("Không", null)
                .show();
    }

    // ⭐️ HÀM NÀY PHẢI GIỮ LẠI BẢN SỬA (CÓ 'boolean success') MÀ CHÚNG TA LÀM TRƯỚC ĐÓ
    private void updateStatus(ReservationModel reservation, ReservationStatus newStatus) {
        int adminUserId = getAdminUserId();
        if (adminUserId == -1) {
            Toast.makeText(getContext(), "Lỗi session Admin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            boolean success = dbHelper.updateReservationStatus((int) reservation.id, newStatus, adminUserId);

            if (success) {
                Toast.makeText(getContext(), "Đã " + newStatus.name(), Toast.LENGTH_SHORT).show();
                loadReservations();
            } else {
                Toast.makeText(getContext(), "Xác nhận thất bại! Đã hết bàn trống.", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // (Hàm getAdminUserId của bạn giữ nguyên)
    private int getAdminUserId() {
        if (getActivity() == null) return -1;
        SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }
}