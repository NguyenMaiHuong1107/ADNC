package com.example.qldb.ActiVity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qldb.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    private static final int COLOR_RED  = Color.parseColor("#FF3B30");
    private static final int COLOR_GRAY = Color.parseColor("#C7CAD1");

    private final int MIN_ADULT = 1, MAX_ADULT = 20;
    private final int MIN_CHILD = 0, MAX_CHILD = 20;

    private int adult = 1;   // sẽ sync lại từ UI ngay khi wire
    private int child = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_booking); // layout chứa các id bạn đã gửi

        // nút Tiếp tục demo
        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            Toast.makeText(this, "Đặt chỗ thành công!", Toast.LENGTH_SHORT).show();
            finish();
        });

        wireBookingScreen();
    }

    private void wireBookingScreen() {
        // ==== Lấy view ====
        TextView tvAdult = findViewById(R.id.tvAdultCount);
        TextView tvChild = findViewById(R.id.tvChildCount);

        MaterialButton btnAdultMinus = findViewById(R.id.btnAdultMinus);
        MaterialButton btnAdultPlus  = findViewById(R.id.btnAdultPlus);
        MaterialButton btnChildMinus = findViewById(R.id.btnChildMinus);
        MaterialButton btnChildPlus  = findViewById(R.id.btnChildPlus);

        TextInputEditText edtDate   = findViewById(R.id.edtDate);
        TextInputEditText edtTime   = findViewById(R.id.edtTime);
        TextInputEditText edtPhone  = findViewById(R.id.edtPhone);
        TextInputEditText edtEmail  = findViewById(R.id.edtEmail);
        TextInputEditText edtContact= findViewById(R.id.edtContactName);
        View btnContinue            = findViewById(R.id.btnContinue);

        // ==== Đồng bộ số lượng từ UI ====
        adult = safeParse(tvAdult.getText().toString(), Math.max(1, MIN_ADULT));
        child = safeParse(tvChild.getText().toString(), Math.max(0, MIN_CHILD));
        tvAdult.setText(String.valueOf(adult));
        tvChild.setText(String.valueOf(child));

        // ==== TỰ ĐIỀN NGÀY / GIỜ HIỆN TẠI ====
        fillNowIfEmpty(edtDate, edtTime);

        // ==== Cập nhật màu nút +/- ban đầu ====
        updateButtonsColor(btnAdultMinus, btnAdultPlus, btnChildMinus, btnChildPlus);

        // ==== Xử lý +/- người lớn ====
        btnAdultMinus.setOnClickListener(v -> {
            if (adult > MIN_ADULT) {
                adult--;
                tvAdult.setText(String.valueOf(adult));
                updateButtonsColor(btnAdultMinus, btnAdultPlus, btnChildMinus, btnChildPlus);
            }
        });
        btnAdultPlus.setOnClickListener(v -> {
            if (adult < MAX_ADULT) {
                adult++;
                tvAdult.setText(String.valueOf(adult));
                updateButtonsColor(btnAdultMinus, btnAdultPlus, btnChildMinus, btnChildPlus);
            }
        });

        // ==== Xử lý +/- trẻ em ====
        btnChildMinus.setOnClickListener(v -> {
            if (child > MIN_CHILD) {
                child--;
                tvChild.setText(String.valueOf(child));
                updateButtonsColor(btnAdultMinus, btnAdultPlus, btnChildMinus, btnChildPlus);
            }
        });
        btnChildPlus.setOnClickListener(v -> {
            if (child < MAX_CHILD) {
                child++;
                tvChild.setText(String.valueOf(child));
                updateButtonsColor(btnAdultMinus, btnAdultPlus, btnChildMinus, btnChildPlus);
            }
        });

        // ==== Mở Date/Time picker khi bấm ====
        if (edtDate != null)  edtDate.setOnClickListener(v -> showDatePicker(edtDate));
        if (edtTime != null)  edtTime.setOnClickListener(v -> showTimePicker(edtTime));

        // ==== Validate đơn giản + confirm ====
        btnContinue.setOnClickListener(v -> {
            String phone = getText(edtPhone);
            String email = getText(edtEmail);
            String name  = getText(edtContact);

            if (phone.isEmpty()) { edtPhone.setError("Vui lòng nhập số điện thoại"); edtPhone.requestFocus(); return; }
            if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Email không hợp lệ"); edtEmail.requestFocus(); return;
            }
            if (name.isEmpty()) { edtContact.setError("Vui lòng nhập tên liên hệ"); edtContact.requestFocus(); return; }

            showConfirmDialog(adult, child, getText(edtDate), getText(edtTime), phone, email, name);
        });
    }

    /** Điền ngay/giờ hiện tại nếu ô đang trống */
    private void fillNowIfEmpty(TextInputEditText edtDate, TextInputEditText edtTime) {
        Calendar cal = Calendar.getInstance();
        String nowDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR));
        String nowTime = String.format(Locale.getDefault(), "%02d:%02d",
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE));

        if (edtDate != null && (edtDate.getText() == null || edtDate.getText().toString().trim().isEmpty())) {
            edtDate.setText(nowDate);
        }
        if (edtTime != null && (edtTime.getText() == null || edtTime.getText().toString().trim().isEmpty())) {
            edtTime.setText(nowTime);
        }
    }

    // ====== Helpers ======
    private int safeParse(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }
    private String getText(TextInputEditText e) { return e == null || e.getText() == null ? "" : e.getText().toString().trim(); }

    private void updateButtonsColor(MaterialButton aMinus, MaterialButton aPlus,
                                    MaterialButton cMinus, MaterialButton cPlus) {
        tintAction(aMinus, adult > MIN_ADULT);
        tintAction(aPlus,  adult < MAX_ADULT);
        tintAction(cMinus, child > MIN_CHILD);
        tintAction(cPlus,  child < MAX_CHILD);
    }

    private void tintAction(MaterialButton btn, boolean canDo) {
        if (btn == null) return;
        if (canDo) {
            btn.setEnabled(true);
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(COLOR_RED));
            btn.setIconTint(android.content.res.ColorStateList.valueOf(Color.WHITE));
        } else {
            btn.setEnabled(false);
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(COLOR_GRAY));
            btn.setIconTint(android.content.res.ColorStateList.valueOf(Color.WHITE));
        }
    }

    // ====== Date/Time pickers ======
    private void showDatePicker(TextInputEditText target) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(
                this,
                (view, y, m, d) -> target.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y)),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dlg.show();
    }

    private void showTimePicker(TextInputEditText target) {
        final Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        TimePickerDialog dlg = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> target.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)),
                h, min, true
        );
        dlg.show();
    }

    // ====== Dialog xác nhận ======
    private void showConfirmDialog(int adult, int child, String date, String time,
                                   String phone, String email, String name) {
        View view = getLayoutInflater().inflate(R.layout.dialog_confirm_booking, null);

        ((TextView) view.findViewById(R.id.tvConfirmAdult)).setText(String.valueOf(adult));
        ((TextView) view.findViewById(R.id.tvConfirmChild)).setText(String.valueOf(child));
        ((TextView) view.findViewById(R.id.tvConfirmDateTime)).setText(date + " - " + time);
        ((TextView) view.findViewById(R.id.tvConfirmPhone)).setText(phone);
        ((TextView) view.findViewById(R.id.tvConfirmEmail)).setText(email);
        ((TextView) view.findViewById(R.id.tvConfirmName)).setText(name);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setView(view)
                .create();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            dialog.dismiss();
// 🔹 Lưu thông tin đặt chỗ vào SharedPreferences
            saveReservationToSharedPrefs(name, date, time);

            Toast.makeText(this, "Đặt chỗ thành công!", Toast.LENGTH_SHORT).show();

            // 🔹 Quay lại màn lịch sử
            Intent intent = new Intent(this, ReservationHistoryActivity.class);
            startActivity(intent);

            finish();


        });

        dialog.show();
    }
    private void saveReservationToSharedPrefs(String restaurantName, String date, String time) {
        android.content.SharedPreferences prefs = getSharedPreferences("reservations", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();

        // Ghép thành một chuỗi, ví dụ: "Vườn nướng BBQ|26/10/2025|11:30;Nhà hàng ABC|27/10/2025|18:00"
        String existing = prefs.getString("data", "");
        String newEntry = restaurantName + "|" + date + "|" + time + ";";
        editor.putString("data", existing + newEntry);
        editor.apply();
    }

}
