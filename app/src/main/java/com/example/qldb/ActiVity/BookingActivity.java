package com.example.qldb.ActiVity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences; // ⭐️ Cần cho việc lấy user_id
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qldb.DatabaseHelper; // ⭐️ Import DatabaseHelper
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

    private int adult = 1;
    private int child = 0;
    private static final int MAX_HOLD_MINUTES    = 20; // 🔔 giữ bàn tối đa 20 phút

    private DatabaseHelper dbHelper; // ⭐️ Khai báo DatabaseHelper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_booking);

        dbHelper = new DatabaseHelper(this); // ⭐️ Khởi tạo DatabaseHelper

        // Xóa code demo cũ
        // findViewById(R.id.btnContinue).setOnClickListener(v -> {
        //     Toast.makeText(this, "Đặt chỗ thành công!", Toast.LENGTH_SHORT).show();
        //     finish();
        // });

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
        TextInputEditText edtNote   = findViewById(R.id.edtNote); // ⭐️ Lấy thêm ghi chú
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
            String notes = getText(edtNote); // ⭐️ Lấy text ghi chú
            String date  = getText(edtDate);
            String time  = getText(edtTime);
            if (phone.isEmpty()) { edtPhone.setError("Vui lòng nhập số điện thoại"); edtPhone.requestFocus(); return; }
            if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Email không hợp lệ"); edtEmail.requestFocus(); return;
            }
            if (name.isEmpty()) { edtContact.setError("Vui lòng nhập tên liên hệ"); edtContact.requestFocus(); return; }

            // ==== Phải đặt trước ít nhất 60 phút ====
            if (!isAtLeastMinutesAhead(date, time, MIN_ADVANCE_MINUTES)) {
                Toast.makeText(this, "Thời gian đặt bàn phải trước ít nhất 60 phút.", Toast.LENGTH_SHORT).show();
                if (edtTime != null) edtTime.requestFocus();
                return;
            }

            // ==== Ràng buộc số người theo chính sách bàn ====
            int eqAdults = toEquivalentAdults(adult, child);

            // 2.1. Nếu ít hơn tối thiểu 5 người lớn (người lớn THỰC TẾ)
            if (adult < MIN_TABLE_ADULTS) {
                String msg = "Số người lớn bạn đặt (" + adult + ") ít hơn tối thiểu (" + MIN_TABLE_ADULTS +
                        "). Nhà hàng chỉ phục vụ 1 bàn từ 5 người lớn trở lên. " +
                        "Đơn của bạn có thể bị ghép bàn với khách khác. Bạn có đồng ý tiếp tục đặt không?";
                showSoftWarning(msg, () -> showConfirmDialog(adult, child, date, time, phone, email, name, notes));
                return;
            }

// 2.2. Nếu vượt quá tối đa 1 bàn (tính theo quy đổi 2 trẻ = 1 lớn)
            if (eqAdults > MAX_TABLE_ADULTS) {
                String msg = "Tổng số người quy đổi (" + eqAdults + ") đã vượt sức chứa tối đa 1 bàn (" + MAX_TABLE_ADULTS + "). " +
                        "Lưu ý: 2 trẻ em được tính như 1 người lớn để đảm bảo chỗ ngồi. " +
                        "Đơn của bạn có thể được chia hoặc ghép bàn khi nhân viên xác nhận. Bạn có đồng ý tiếp tục không?";
                showSoftWarning(msg, () -> showConfirmDialog(adult, child, date, time, phone, email, name, notes));
                return;
            }


            // OK: Qua confirm như bình thường
            showConfirmDialog(adult, child, date, time, phone, email, name, notes);


        });
    }
    // ====== Time helpers ======
    private static final String DATE_FMT = "dd/MM/yyyy";
    private static final String TIME_FMT = "HH:mm";

    private long parseMillis(String date, String time) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FMT + " " + TIME_FMT, java.util.Locale.getDefault());
            sdf.setLenient(false);
            java.util.Date d = sdf.parse(date.trim() + " " + time.trim());
            return (d != null) ? d.getTime() : -1L;
        } catch (Exception e) {
            return -1L;
        }
    }

    /** true nếu (date,time) nằm sau hiện tại ít nhất 'minutes' phút */
    private boolean isAtLeastMinutesAhead(String date, String time, int minutes) {
        long selected = parseMillis(date, time);
        if (selected < 0) return false;
        long now = System.currentTimeMillis();
        long threshold = now + minutes * 60_000L;
        return selected >= threshold;
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
    // ⭐️ Thêm tham số 'notes'
    private void showConfirmDialog(int adult, int child, String date, String time,
                                   String phone, String email, String name, String notes) {

        // ⭐️ Lấy user_id của người đang đăng nhập
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int currentUserId = prefs.getInt("user_id", -1);

        // ⭐️ Kiểm tra xem user_id có hợp lệ không
        if (currentUserId == -1) {
            Toast.makeText(this, "Lỗi: Phiên đăng nhập không hợp lệ.", Toast.LENGTH_SHORT).show();
            // Có thể bạn muốn chuyển họ về trang đăng nhập
            // startActivity(new Intent(this, SignInActivity.class));
            return;
        }


        View view = getLayoutInflater().inflate(R.layout.dialog_confirm_booking, null);

        ((TextView) view.findViewById(R.id.tvConfirmAdult)).setText(String.valueOf(adult));
        ((TextView) view.findViewById(R.id.tvConfirmChild)).setText(String.valueOf(child));
        ((TextView) view.findViewById(R.id.tvConfirmDateTime)).setText(date + " - " + time);
        ((TextView) view.findViewById(R.id.tvConfirmPhone)).setText(phone);
        ((TextView) view.findViewById(R.id.tvConfirmEmail)).setText(email);
        ((TextView) view.findViewById(R.id.tvConfirmName)).setText(name);
        // (Bạn có thể thêm 1 TextView cho 'notes' trong dialog_confirm_booking.xml nếu muốn)

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setView(view)
                .create();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> dialog.dismiss());
        ((TextView) view.findViewById(R.id.tvConfirmHoldNote))
                .setText("Nhà hàng giữ bàn tối đa " + MAX_HOLD_MINUTES
                        + " phút kể từ giờ bạn đặt. Quá thời gian, đặt chỗ có thể bị hủy/ghép bàn.");

        // ⭐️⭐️ ĐÂY LÀ THAY ĐỔI QUAN TRỌNG NHẤT ⭐️⭐️
// ⬇️ ĐÂY LÀ CODE ĐÚNG ⬇️
        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            dialog.dismiss();

            // ⭐️ KHÔNG TÍNH TỔNG NỮA
            // int totalPeople = adult + child;

            // ⭐️ GỌI HÀM VỚI adult VÀ child
            long newId = dbHelper.insertReservationPending(
                    currentUserId, // user_id của người đặt
                    null,          // table_id (chưa chọn bàn)
                    date,          // ngày
                    time,          // giờ
                    // totalPeople,   // ⭐️ ĐÃ XÓA
                    adult,         // ⭐️ GỬI adult
                    child,         // ⭐️ GỬI child
                    notes          // ghi chú
            );

            if (newId > 0) {
                Toast.makeText(this, "Đặt chỗ thành công!", Toast.LENGTH_SHORT).show();

                // 🔹 Quay lại màn lịch sử
                Intent intent = new Intent(this, ReservationHistoryActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi đặt chỗ. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    // ⭐️ Hàm này không còn cần thiết cho việc đặt chỗ nữa
    // (Bạn có thể giữ nó nếu ReservationHistoryActivity vẫn đang dùng,
    // nhưng TỐT NHẤT là nên xóa nó và sửa ReservationHistoryActivity)
    private void saveReservationToSharedPrefs(String restaurantName, String date, String time) {
        // ...
    }
    // ====== Booking constraints ======
    private static final int MIN_ADVANCE_MINUTES = 60; // phải đặt trước ít nhất 60'
    private static final int MIN_TABLE_ADULTS    = 5;  // tối thiểu 5 người lớn (thực tế)
    private static final int MAX_TABLE_ADULTS    = 8;  // tối đa 8 người lớn (quy đổi, 2 trẻ = 1 lớn)

    /** Quy đổi số ghế kiểm tra trần tối đa: 2 trẻ = 1 lớn (làm tròn lên) */
    private int toEquivalentAdults(int adult, int child) {
        int childAsAdult = (child + 1) / 2; // ceil(child/2)
        return adult + childAsAdult;
    }

    /** Hộp thoại cảnh báo mềm, người dùng đồng ý thì chạy onYes.run() */
    private void showSoftWarning(String message, Runnable onYes) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Lưu ý")
                .setMessage(message)
                .setPositiveButton("Đồng ý", (d, w) -> {
                    d.dismiss();
                    if (onYes != null) onYes.run();
                })
                .setNegativeButton("Không", (d, w) -> d.dismiss())
                .show();
    }

}