package com.example.qldb.ActiVity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.qldb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class AdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    // Khai báo các Fragment
    final Fragment fragmentHome = new AdminHomeFragment();
    final Fragment fragmentReservations = new AdminReservationsFragment();
    // ⭐️ SỬA 1: Khai báo Fragment "Người dùng"
    final Fragment fragmentUsers = new AdminUsersFragment();
    // final Fragment fragmentAccount = new AdminAccountFragment();

    final FragmentManager fm = getSupportFragmentManager();
    Fragment active = fragmentHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // 1. Kiểm tra quyền Admin (Security Check)
        if (!isAdmin()) {
            Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
            logout(); // Đuổi về trang đăng nhập
            return;
        }

        bottomNav = findViewById(R.id.admin_bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        // Thêm các fragment vào FragmentManager
        // .hide() để chúng không bị chồng chéo
        fm.beginTransaction().add(R.id.admin_fragment_container, fragmentHome, "1").commit();
        fm.beginTransaction().add(R.id.admin_fragment_container, fragmentReservations, "2").hide(fragmentReservations).commit();
        // ⭐️ SỬA 2: Thêm fragment "Người dùng" vào FragmentManager
        fm.beginTransaction().add(R.id.admin_fragment_container, fragmentUsers, "3").hide(fragmentUsers).commit();


        // Load Fragment "Trang chủ" (Home) làm mặc định
        loadFragment(fragmentHome);
    }

    // Xử lý sự kiện khi click item trên BottomNav
    private final NavigationBarView.OnItemSelectedListener navListener =
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_admin_home) {
                        loadFragment(fragmentHome);
                        return true;
                    } else if (itemId == R.id.nav_admin_reservations) {
                        loadFragment(fragmentReservations);
                        return true;
                    } else if (itemId == R.id.nav_admin_users) {
                        // ⭐️ SỬA 3: Load fragment "Người dùng" khi bấm
                        loadFragment(fragmentUsers);
                        // Toast.makeText(AdminActivity.this, "Chức năng Người dùng", Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (itemId == R.id.nav_admin_account) {
                        // (Load fragment tài khoản & nút logout ở đây)
                        Toast.makeText(AdminActivity.this, "Chức năng Tài khoản", Toast.LENGTH_SHORT).show();
                        // loadFragment(fragmentAccount);
                        // Tạm thời xử lý logout ở đây
                        logout();
                        return true;
                    }
                    return false;
                }
            };

    // Hàm helper để đổi Fragment (ẩn fragment cũ, hiện fragment mới)
    private void loadFragment(Fragment fragment) {
        fm.beginTransaction().hide(active).show(fragment).commit();
        active = fragment;
    }

    // --- Các hàm hỗ trợ từ Activity cũ ---

    private boolean isAdmin() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String role = prefs.getString("user_role", "user"); // Mặc định là user
        return "admin".equals(role);
    }

    private void logout() {
        // Xóa session
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Về trang Đăng nhập
        Intent intent = new Intent(AdminActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}