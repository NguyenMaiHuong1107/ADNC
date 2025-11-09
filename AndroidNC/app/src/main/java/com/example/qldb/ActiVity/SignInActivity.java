package com.example.qldb.ActiVity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qldb.DatabaseHelper;
import com.example.qldb.R;
import com.example.qldb.UserModel;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignInActivity extends AppCompatActivity {

    private EditText etPhone, etPassword;
    private Button btnSignIn;
    private TextView tvSignUpLink;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Khởi tạo các thành phần giao diện
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvSignUpLink = findViewById(R.id.tvSignUpLink);

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Xử lý nút Đăng nhập
        btnSignIn.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validation
            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Băm mật khẩu trước khi kiểm tra
            String hashedPassword = hashPassword(password);
            Log.d("SignInActivity", "Phone: " + phone + ", HashedPassword: " + hashedPassword);

            // Kiểm tra đăng nhập
            boolean isValid = dbHelper.checkUser(phone, hashedPassword);

            if (isValid) {
                int userId = dbHelper.getUserId(phone, hashedPassword);
                if (userId != -1) {
                    // ✅ Lấy thông tin người dùng từ DB (dùng hàm bạn đã có)
                    UserModel u = dbHelper.getUserById(userId);

                    // ✅ Lưu cả user_id + full_name + phone vào SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("user_id", userId);
                    if (u != null) {
                        editor.putString("full_name", u.fullName);
                        editor.putString("phone", u.phone);
                    }
                    editor.apply();

                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignInActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Số điện thoại hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }

        });

        // Liên kết sang màn hình Đăng ký
        tvSignUpLink.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            finish();
        });
    }

    // Phương thức băm mật khẩu SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback nếu lỗi (không nên dùng trong production)
        }
    }
}