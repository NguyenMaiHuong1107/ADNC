package com.example.qldb.ActiVity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qldb.R;

import java.util.ArrayList;
import java.util.List;

public class ReservationHistoryActivity extends AppCompatActivity {

    private ReservationHistoryAdapter adapter;
    private List<Reservation> allReservations = new ArrayList<>();
    private RecyclerView recyclerView;
    private Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_history);


        spinner = findViewById(R.id.spinnerStatus);
        recyclerView = findViewById(R.id.recyclerReservation);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // 🔹 Lấy toàn bộ dữ liệu từ SharedPreferences
        allReservations = getReservationsFromSharedPrefs();


        // 🔹 Adapter hiển thị mặc định “Chưa xác nhận”
        adapter = new ReservationHistoryAdapter(allReservations);
        recyclerView.setAdapter(adapter);


        // 🔹 Danh sách trạng thái hiển thị trong Spinner
        String[] statusList = {"Chưa xác nhận", "Đã xác nhận", "Đã hủy", "Hoàn thành"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                statusList
        );

        spinner.setAdapter(spinnerAdapter);


        // 🔹 Mặc định chọn “Chưa xác nhận”
        spinner.setSelection(0);
        updateReservationList("Chưa xác nhận");


        // 🔹 Khi chọn trạng thái khác trong Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String selected = statusList[position];
                updateReservationList(selected);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


    /** Lọc danh sách hiển thị theo trạng thái được chọn */
    private void updateReservationList(String status) {
        List<Reservation> filtered = new ArrayList<>();


        for (Reservation r : allReservations) {
            if (r.getStatus().equals(status)) {
                filtered.add(r);
            }
        }


        adapter.updateList(filtered);
    }



    /** 💾 Lấy dữ liệu từ SharedPreferences */
    private List<Reservation> getReservationsFromSharedPrefs() {
        List<Reservation> list = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("reservations", MODE_PRIVATE);
        String data = prefs.getString("data", "");


        if (!data.isEmpty()) {
            // Ví dụ: tenQuan|ngay|gio|trangThai;
            String[] entries = data.split(";");
            for (String e : entries) {
                if (e.trim().isEmpty()) continue;
                String[] parts = e.split("\\|");
                // Nếu có trạng thái -> đọc đúng
                // Nếu chưa có -> gán “Chưa xác nhận”
                String status = (parts.length >= 4) ? parts[3] : "Chưa xác nhận";


                list.add(new Reservation(
                        parts[0],
                        parts[1],
                        parts[2],
                        status,
                        R.drawable.sample_restaurant
                ));
            }
        }


        return list;
    }


}



