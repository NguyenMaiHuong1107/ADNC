package com.example.qldb.ActiVity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qldb.R;
import com.example.qldb.ActiVity.Reservation;

import java.util.List;

public class ReservationHistoryAdapter extends RecyclerView.Adapter<ReservationHistoryAdapter.ViewHolder> {

    private List<Reservation> reservationList;

    public ReservationHistoryAdapter(List<Reservation> reservationList) {
        this.reservationList = reservationList;
    }

    public void updateList(List<Reservation> newList) {
        reservationList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservationHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationHistoryAdapter.ViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);
        holder.tvRestaurantName.setText(reservation.getRestaurantName());
        holder.tvDate.setText(reservation.getDate());
        holder.tvTime.setText(reservation.getTime());
        holder.imgRestaurant.setImageResource(reservation.getImageResId());
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRestaurant;
        TextView tvRestaurantName, tvDate, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRestaurant = itemView.findViewById(R.id.imgRestaurant);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
