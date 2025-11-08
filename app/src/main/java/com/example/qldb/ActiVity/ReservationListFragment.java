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

public class ReservationListFragment extends Fragment implements AdminReservationAdapter.OnActionClickListener {

    private static final String ARG_STATUSES = "statuses";

    private ListView lvReservations;
    private DatabaseHelper dbHelper;
    private AdminReservationAdapter adapter;
    private List<ReservationModel> reservationList;
    private ArrayList<String> statusesToShow;

    /**
     * Tạo Fragment mới và truyền danh sách trạng thái (ví dụ: ["pending"])
     */
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

        // Sẽ được gọi lại trong onResume, nhưng gọi 1 lần ở đây để tải lần đầu
        loadReservations();
    }

    // ⭐️ FIX 1: THÊM LẠI onResume ĐỂ TỰ ĐỘNG LÀM MỚI
    @Override
    public void onResume() {
        super.onResume();

        // Kiểm tra xem view đã được tạo chưa trước khi tải
        // Điều này đảm bảo list tự làm mới khi chuyển tab hoặc sau khi
        // xác nhận/xóa
        if (getView() != null) {
            loadReservations();
        }
    }


    private void loadReservations() {
        if (statusesToShow == null || statusesToShow.isEmpty()) return;
        if (getContext() == null) return; // Tránh crash nếu context null

        // Lấy data từ DB theo trạng thái đã truyền
        reservationList = dbHelper.getReservationsByStatusList(statusesToShow);

        // Nếu trạng thái là PENDING, hiện nút
        boolean showActions = statusesToShow.contains(ReservationStatus.PENDING.getValue());

        adapter = new AdminReservationAdapter(getContext(), reservationList, showActions);

        // Chỉ set listener nếu chúng ta hiện nút
        if (showActions) {
            adapter.setOnActionClickListener(this);
        }

        lvReservations.setAdapter(adapter);
    }

    // --- Đây là logic xử lý khi bấm (✓) hoặc (✗) ---

    @Override
    public void onConfirmClick(ReservationModel reservation) {
        showConfirmationDialog(reservation, ReservationStatus.CONFIRMED, "Xác nhận đơn này?");
    }

    @Override
    public void onCancelClick(ReservationModel reservation) {
        // Dòng này sẽ gọi dialog HỎI XÓA
        showDeleteDialog(reservation);
    }

    // ⭐️ FIX 2: SỬA LẠI LOGIC NÚT "XÓA"
    private void showDeleteDialog(ReservationModel reservation) {
        if (getContext() == null) return; // Tránh crash

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa đơn: ".concat(String.valueOf(reservation.id)))
                .setMessage("Bạn có chắc muốn XÓA vĩnh viễn đơn này?\nKhách: " + reservation.getContactInfo())
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Gọi hàm helper "deleteReservation" ngay bên dưới
                    // (Không gọi dbHelper.deleteReservation)
                    deleteReservation(reservation);
                })
                .setNegativeButton("Không", null)
                .setIcon(android.R.drawable.ic_dialog_alert) // Thêm icon cảnh báo
                .show();
    }

    /**
     * Hàm helper này thực thi việc xóa
     */
    private void deleteReservation(ReservationModel reservation) {
        try {
            // Gọi hàm xóa trong DB (mà chúng ta đã thêm vào DatabaseHelper)
            dbHelper.deleteReservation(reservation.id);
            Toast.makeText(getContext(), "Đã xóa đơn " + reservation.id, Toast.LENGTH_SHORT).show();

            // Tải lại danh sách ngay lập tức
            loadReservations();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Hàm này dùng để Xác nhận (nút ✓)
     */
    private void showConfirmationDialog(ReservationModel reservation, ReservationStatus newStatus, String message) {
        if (getContext() == null) return; // Tránh crash

        new AlertDialog.Builder(getContext())
                .setTitle("Xử lý đơn: ".concat(String.valueOf(reservation.id)))
                .setMessage(message + "\nKhách: " + reservation.getContactInfo())
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    updateStatus(reservation, newStatus);
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void updateStatus(ReservationModel reservation, ReservationStatus newStatus) {
        int adminUserId = getAdminUserId();
        if (adminUserId == -1) {
            Toast.makeText(getContext(), "Lỗi session Admin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            dbHelper.updateReservationStatus(reservation.id, newStatus, adminUserId);
            Toast.makeText(getContext(), "Đã " + newStatus.name(), Toast.LENGTH_SHORT).show();

            // Tải lại danh sách ngay lập tức
            loadReservations();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private int getAdminUserId() {
        if (getActivity() == null) return -1; // Tránh crash
        SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }
}