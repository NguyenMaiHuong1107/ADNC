package com.example.qldb.ActiVity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qldb.DatabaseHelper;
import com.example.qldb.R;

public class AccountActivity extends AppCompatActivity {

    private TextView txtFullName, txtPhone;
    private Button btnLogout;
    private DatabaseHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Ánh xạ view
        txtFullName = findViewById(R.id.txtFullName);
        txtPhone    = findViewById(R.id.txtPhone);
        btnLogout   = findViewById(R.id.btnLogout);

        dbHelper = new DatabaseHelper(this);

        // ---- Đọc session ----
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            // Chưa đăng nhập -> về SignIn và clear back stack
            goToSignIn();
            return;
        }

        // Ưu tiên cache để hiển thị nhanh
        String cachedName  = prefs.getString("full_name", "");
        String cachedPhone = prefs.getString("phone", "");
        if (!cachedName.isEmpty())  txtFullName.setText(cachedName);
        if (!cachedPhone.isEmpty()) txtPhone.setText(cachedPhone);

        // Nếu cache trống (lần đăng nhập đầu / chưa kịp set) -> load DB
        if (cachedName.isEmpty() || cachedPhone.isEmpty()) {
            loadUserInfo(userId); // hàm này sẽ set UI và đồng thời cập nhật lại cache
        }

        // ---- Đăng xuất ----
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(AccountActivity.this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
            goToSignIn();
        });

        // Mở trang Cài đặt
        LinearLayout layoutAdd = findViewById(R.id.layoutAdd);
        layoutAdd.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, SettingActivity.class)));


        LinearLayout rowBookingHistory = findViewById(R.id.rowBookingHistory);
        rowBookingHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Đã bấm Lịch sử", Toast.LENGTH_SHORT).show();
            android.util.Log.d("AccountActivity","Row booking history clicked");

            Intent i = new Intent(AccountActivity.this, ReservationHistoryActivity.class);
            startActivity(i);
        });

    }

    // Hàm lấy dữ liệu từ DB và set lên TextView + cập nhật cache
    private void loadUserInfo(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT full_name, phone FROM Users WHERE user_id = ?",
                new String[]{ String.valueOf(userId) }
        );

        if (cursor.moveToFirst()) {
            String fullName = cursor.getString(0);
            String phone    = cursor.getString(1);

            txtFullName.setText(fullName != null ? fullName : "");
            txtPhone.setText(phone != null ? phone : "");

            // Cập nhật lại cache để lần sau mở nhanh
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            prefs.edit()
                    .putString("full_name", fullName != null ? fullName : "")
                    .putString("phone",    phone    != null ? phone    : "")
                    .apply();

            Log.d("AccountActivity", "Refreshed from DB: name=" + fullName + ", phone=" + phone);
        } else {
            Log.d("AccountActivity", "No user found for user_id: " + userId);
            Toast.makeText(this, "Không tìm thấy người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            goToSignIn();
        }
        cursor.close();
        db.close();
    }

    private void goToSignIn() {
        Intent intent = new Intent(AccountActivity.this, SignInActivity.class);
        // Clear back stack để không back về Account sau khi đăng xuất
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
