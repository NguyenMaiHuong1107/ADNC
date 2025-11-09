package com.example.qldb.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.qldb.R;
import com.example.qldb.ReservationModel;

import java.util.List;
import java.util.Locale;

public class AdminReservationAdapter extends ArrayAdapter<ReservationModel> {

    private final List<ReservationModel> reservationList;
    private final Context mContext;
    private OnActionClickListener actionClickListener;

    // ⭐️ SỬA LẠI: Thêm 2 boolean để kiểm soát nút
    private final boolean showPendingActions; // Hiện nút Confirm/Cancel
    private final boolean showFinishAction;   // Hiện nút "Ăn xong"

    // ⭐️ Interface listener (THÊM 1 HÀM MỚI)
    public interface OnActionClickListener {
        void onConfirmClick(ReservationModel reservation);
        void onCancelClick(ReservationModel reservation);
        void onFinishClick(ReservationModel reservation); // ⭐️ HÀM MỚI
    }

    public void setOnActionClickListener(OnActionClickListener listener) {
        this.actionClickListener = listener;
    }

    // ⭐️ SỬA CONSTRUCTOR
    public AdminReservationAdapter(@NonNull Context context, List<ReservationModel> list,
                                   boolean showPendingActions, boolean showFinishAction) {
        super(context, 0, list);
        this.mContext = context;
        this.reservationList = list;
        this.showPendingActions = showPendingActions;
        this.showFinishAction = showFinishAction;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item_admin_reservation, parent, false);
        }

        ReservationModel currentReservation = reservationList.get(position);

        // --- 1. SỬA LỖI HIỂN THỊ THÔNG TIN (REQUEST 3) ---
        TextView tvName = listItem.findViewById(R.id.tvCustomerName);
        TextView tvPhone = listItem.findViewById(R.id.tvCustomerPhone);
        TextView tvGuestCount = listItem.findViewById(R.id.tvGuestCount);
        TextView tvDateTime = listItem.findViewById(R.id.tvDateTime);

        // Lấy dữ liệu (an toàn, tránh lỗi "null")
        String name = (currentReservation.contactName != null) ? currentReservation.contactName : "Không có tên";
        String phone = (currentReservation.phone != null) ? currentReservation.phone : "Không có SĐT";

        // Tính tổng số người
        int adults = currentReservation.numAdults;
        int children = currentReservation.numChildren;
        int totalGuests = adults + children;

        // Định dạng chuỗi hiển thị
        String guestText = String.format(Locale.getDefault(), "Số lượng: %d người (%dL, %dTE)", totalGuests, adults, children);
        String dateTimeText = String.format("Ngày: %s - Giờ: %s", currentReservation.date, currentReservation.time);

        // Set text
        tvName.setText(name);
        tvPhone.setText(phone);
        tvGuestCount.setText(guestText);
        tvDateTime.setText(dateTimeText);

        // --- 2. XỬ LÝ CÁC NÚT HÀNH ĐỘNG (REQUEST 1) ---
        LinearLayout llPendingActions = listItem.findViewById(R.id.llPendingActions);
        Button btnFinishDining = listItem.findViewById(R.id.btnFinishDining);

        Button btnConfirm = listItem.findViewById(R.id.btnConfirm);
        Button btnCancel = listItem.findViewById(R.id.btnCancel);

        // Ẩn/Hiện nhóm nút "Chờ xác nhận"
        if (showPendingActions) {
            llPendingActions.setVisibility(View.VISIBLE);

            btnConfirm.setOnClickListener(v -> {
                if (actionClickListener != null) {
                    actionClickListener.onConfirmClick(currentReservation);
                }
            });

            btnCancel.setOnClickListener(v -> {
                if (actionClickListener != null) {
                    actionClickListener.onCancelClick(currentReservation);
                }
            });

        } else {
            llPendingActions.setVisibility(View.GONE);
        }

        // Ẩn/Hiện nút "Ăn xong"
        if (showFinishAction) {
            btnFinishDining.setVisibility(View.VISIBLE);

            btnFinishDining.setOnClickListener(v -> {
                if (actionClickListener != null) {
                    actionClickListener.onFinishClick(currentReservation); // ⭐️ GỌI HÀM MỚI
                }
            });
        } else {
            btnFinishDining.setVisibility(View.GONE);
        }

        return listItem;
    }
}