package com.example.qldb;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.qldb.ActiVity.BookingActivity;
import com.example.qldb.R;
import com.google.android.material.button.MaterialButton;

public class QuyDinhFragment extends Fragment {
    private boolean isExpanded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_quy_dinh, container, false);
        TextView tvQuyDinh = v.findViewById(R.id.tvQuyDinh);
        TextView btnXemThem = v.findViewById(R.id.btnXemThem);

        // --- Nội dung Quy định (HTML để dễ in đậm + xuống dòng) ---
        String html = "<b>Đề xe</b><br><br>" +
                "<b>1. Chỗ để ô tô</b><br>" +
                "- Nơi để: Để xe ở hầm gửi xe<br>" +
                "- Phí trông giữ xe: 5.000đ/1 lượt<br><br>" +
                "<b>2. Chỗ để xe máy</b><br>" +
                "- Nơi để: Để xe ở hầm gửi xe<br>" +
                "- Phí trông giữ xe: 5.000đ/1 lượt<br><br>" +
                "<b>Tiện ích:</b><br>" +
                "✅ Máy chiếu &nbsp;&nbsp;&nbsp;&nbsp; ❌ MC dẫn chương trình<br>" +
                "✅ Âm thanh - ánh sáng &nbsp;&nbsp;&nbsp;&nbsp; ❌ Chỗ để ô tô miễn phí<br>" +
                "✅ HD trực tiếp &nbsp;&nbsp;&nbsp;&nbsp; ❌ Bóng đá K+<br><br>" +
                "<b>Ghi chú thêm:</b><br>" +
                "• Không mang đồ ăn, thức uống từ ngoài vào.<br>" +
                "• Vui lòng giữ trật tự, không gây ồn ào trong khu vực ăn uống.<br>" +
                "• Nhà hàng phục vụ từ 10:00 - 22:00 mỗi ngày.";

        tvQuyDinh.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));

        // Ban đầu ẩn bớt phần cuối
        tvQuyDinh.setMaxLines(12);
        MaterialButton btnBookNow = v.findViewById(R.id.btnBookNow);
        btnBookNow.setOnClickListener(view -> {
            Intent i = new Intent(getActivity(), BookingActivity.class);
            startActivity(i);
        });


        // --- Xử lý nút Xem thêm / Thu gọn ---
        btnXemThem.setOnClickListener(view -> {
            if (isExpanded) {
                tvQuyDinh.setMaxLines(12);
                btnXemThem.setText("Xem thêm");
                isExpanded = false;
            } else {
                tvQuyDinh.setMaxLines(Integer.MAX_VALUE);
                btnXemThem.setText("Thu gọn");
                isExpanded = true;
            }

        });

        return v;
    }
}

