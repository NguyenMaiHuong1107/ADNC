package com.example.qldb.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.qldb.R;
import com.example.qldb.ReservationModel;
import java.util.List;

public class AdminReservationAdapter extends ArrayAdapter<ReservationModel> {

    private final boolean showActions;
    private OnActionClickListener actionClickListener;

    /**
     * Interface để báo cho Fragment biết khi nào nút (✓) hoặc (✗) được bấm.
     */
    public interface OnActionClickListener {
        void onConfirmClick(ReservationModel reservation);
        void onCancelClick(ReservationModel reservation);
    }

    public void setOnActionClickListener(OnActionClickListener listener) {
        this.actionClickListener = listener;
    }

    public AdminReservationAdapter(@NonNull Context context, @NonNull List<ReservationModel> objects, boolean showActions) {
        super(context, 0, objects);
        this.showActions = showActions; // true nếu là tab PENDING, false cho các tab khác
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.admin_reservation_card_item, parent, false);
        }

        ReservationModel currentReservation = getItem(position);

        // Ánh xạ các view trong card
        TextView tvContact = listItemView.findViewById(R.id.tvItemAdminContact);
        TextView tvDateTime = listItemView.findViewById(R.id.tvItemAdminDateTime);
        TextView tvPeople = listItemView.findViewById(R.id.tvItemAdminPeople);
        LinearLayout actionLayout = listItemView.findViewById(R.id.action_layout);
        ImageButton btnConfirm = listItemView.findViewById(R.id.btnItemConfirm);
        ImageButton btnCancel = listItemView.findViewById(R.id.btnItemCancel);

        if (currentReservation != null) {
            // Set data
            tvContact.setText(currentReservation.getContactInfo());
            tvDateTime.setText(currentReservation.getDateTimeInfo());

            int totalPeople = currentReservation.numAdults + currentReservation.numChildren;
            tvPeople.setText("Số lượng: " + totalPeople + " người");

            // Ẩn/hiện cụm nút ✓/✗
            if (showActions) {
                actionLayout.setVisibility(View.VISIBLE);

                // Gán sự kiện click cho nút
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
                // Nếu đây là tab "Đã xác nhận", ẩn các nút đi
                actionLayout.setVisibility(View.GONE);
            }
        }
        return listItemView;
    }
}