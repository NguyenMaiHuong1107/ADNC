package com.example.qldb.ActiVity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.qldb.R;
import com.example.qldb.ActiVity.Reservation;

import java.util.ArrayList;
import java.util.List;

public class ReservationViewModel extends ViewModel {
    private final MutableLiveData<List<Reservation>> reservationList = new MutableLiveData<>();

    public LiveData<List<Reservation>> getReservationList() {
        return reservationList;
    }

    public void loadReservations(String status) {
        List<Reservation> list = new ArrayList<>();

        switch (status) {
            case "Chờ xác nhận":
                list.add(new Reservation("Vườn nướng BBQ - S101 Vin", "26/10/2025", "11:30", status, R.drawable.sample_restaurant));
                break;
            case "Đã xác nhận":
                list.add(new Reservation("Vườn nướng BBQ - S101 Vin", "27/10/2025", "19:00", status, R.drawable.sample_restaurant));
                break;
            case "Đã hủy":
                list.add(new Reservation("Vườn nướng BBQ - S101 Vin", "25/10/2025", "—", status, R.drawable.sample_restaurant));
                break;
            case "Hoàn thành":
                list.add(new Reservation("Vườn nướng BBQ - S101 Vin", "24/10/2025", "12:00", status, R.drawable.sample_restaurant));
                break;
        }

        reservationList.setValue(list);
    }
}
