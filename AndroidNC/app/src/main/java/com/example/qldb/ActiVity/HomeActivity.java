package com.example.qldb.ActiVity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.qldb.Adapter.RestaurantPagerAdapter;
import com.example.qldb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

public class HomeActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private FrameLayout contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState);
        setContentView(R.layout.activity_home); // 👉 Gọi giao diện activity_home.xml
        contentLayout = findViewById(R.id.content);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Khi mới mở -> load giao diện Home
        loadLayout(R.layout.activity_home);
        wireHomeScreen(); // gắn click "Đặt chỗ ngay"
        wireHomeTabs();
        updatePeopleTimeDate();   // ⬅️ cập nhật giờ/ngày sau khi inflate

        // Khi bấm icon trên menu
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadLayout(R.layout.activity_home);
                wireHomeScreen();
                wireHomeTabs();
                updatePeopleTimeDate();
            } else if (id == R.id.nav_booking) {
                startActivity(new Intent(this, BookingActivity.class));
            } else if (id == R.id.nav_add) {
                loadLayout(R.layout.activity_setting);
            } else if (id == R.id.nav_account) {
                loadLayout(R.layout.activity_account);
                wireAccountScreen();
            }
            return true;

        });
        wireHomeScreen();
        wireHomeTabs();
        updatePeopleTimeDate();
    }

    private void loadLayout(int layoutRes) {
        contentLayout.removeAllViews();
        LayoutInflater.from(this).inflate(layoutRes, contentLayout, true);

        // Nếu layout vừa inflate cũng có BottomNavigationView (bị trùng),
        // thì tháo nó ra để trên màn hình chỉ còn 1 menu (cái ở activity_home gốc).
        View dupNav = contentLayout.findViewById(R.id.bottomNav);
        if (dupNav != null && dupNav.getParent() instanceof ViewGroup) {
            ((ViewGroup) dupNav.getParent()).removeView(dupNav);
        }
    }
    private void wireHomeTabs() {
        TabLayout tabLayout = contentLayout.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = contentLayout.findViewById(R.id.viewPager);
        if (tabLayout == null || viewPager == null) return;

        viewPager.setAdapter(new RestaurantPagerAdapter(this));
        new com.google.android.material.tabs.TabLayoutMediator(
                tabLayout, viewPager, (tab, pos) -> {
            switch (pos) {
                case 0: tab.setText("Đề xuất"); break;
                case 1: tab.setText("Tóm tắt"); break;
                case 2: tab.setText("Bảng giá"); break;
                case 3: tab.setText("Quy định"); break;
            }
        }).attach();
    }
    private void updatePeopleTimeDate() {
        TextView tv = contentLayout.findViewById(R.id.tvPeopleTimeDate); // ⬅️ lấy từ contentLayout
        if (tv == null) return; // không có view này trong layout hiện tại

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat fTime = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        java.text.SimpleDateFormat fDate = new java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault());

        String current = "👤 2   •  🕙 " + fTime.format(cal.getTime()) + "  •  📅 " + fDate.format(cal.getTime());
        tv.setText(current);
    }

    // Sau khi load layout_home, gọi hàm này
    private void wireHomeScreen() {
        // nên tìm trong contentLayout để chắc chắn đúng view vừa inflate
        View btnBookNow = contentLayout.findViewById(R.id.btnBookNow);
        if (btnBookNow != null) {
            btnBookNow.setOnClickListener(v -> {
                Intent i = new Intent(HomeActivity.this, BookingActivity.class);

                // (tuỳ chọn) truyền dữ liệu sang màn đặt chỗ
                // i.putExtra("adult_default", 2);
                // i.putExtra("child_default", 0);

                startActivity(i);
            });
        }
    }

    // HomeActivity.java
    private void wireAccountScreen() {
        // Tìm view trong contentLayout (chính là layout_account vừa inflate)
        TextView txtFullName = contentLayout.findViewById(R.id.txtFullName);
        TextView txtPhone    = contentLayout.findViewById(R.id.txtPhone);
        Button btnLogout   = contentLayout.findViewById(R.id.btnLogout);

        LinearLayout layoutAdd         = contentLayout.findViewById(R.id.layoutAdd);          // Cài đặt
        LinearLayout rowBookingHistory = contentLayout.findViewById(R.id.rowBookingHistory);  // Lịch sử

        // ---- Hiển thị tên/điện thoại từ cache (nếu muốn) ----
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String cachedName  = prefs.getString("full_name", "");
        String cachedPhone = prefs.getString("phone", "");
        if (txtFullName != null) txtFullName.setText(cachedName);
        if (txtPhone    != null) txtPhone.setText(cachedPhone);

        // ---- Lịch sử đơn đặt chỗ ----
        if (rowBookingHistory != null) {
            rowBookingHistory.setClickable(true);
            rowBookingHistory.setFocusable(true);
            rowBookingHistory.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, ReservationHistoryActivity.class));
            });
        }

        // ---- Cài đặt ----
        if (layoutAdd != null) {
            layoutAdd.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, SettingActivity.class))
            );
        }

        // ---- ĐĂNG XUẤT (đặt ở HomeActivity vì layout đang hiển thị trong HomeActivity) ----
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                // Xoá session
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                Toast.makeText(HomeActivity.this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();

                // Chuyển về màn Đăng nhập và clear back stack
                Intent i = new Intent(HomeActivity.this, SignInActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish(); // đóng HomeActivity
            });
        }
    }


}