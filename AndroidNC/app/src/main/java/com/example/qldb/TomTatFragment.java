package com.example.qldb;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class TomTatFragment extends Fragment {
    private boolean isExpanded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tom_tat, container, false);

        TextView tvTomTat = v.findViewById(R.id.tvTomTat);
        TextView btnToggle = v.findViewById(R.id.btnXemThemTomTat);

        // Nội dung tóm tắt – dùng HTML để in đậm tiêu đề mục
        String html = "<b>Tóm tắt Vườn nướng BBQ - S101 Vinhomes Smart City</b><br><br>" +
                "<b>Phù hợp:</b><br>" +
                "• Ăn gia đình, tụ tập bạn bè, liên hoan văn phòng, họp nhóm,<br>" +
                "• Tổ chức sinh nhật, tiệc công ty, tiếp khách, tiệc thôi nôi, kỷ niệm ngày đặc biệt,...<br><br>" +
                "<b>Món đặc sắc:</b><br>" +
                "• Tôm sú nướng, nầm nướng sate, má heo mềm, ba chỉ bò cuốn nấm,<br>" +
                "• Ba chỉ heo Hàn, cánh gà BBQ, hàu nướng mỡ hành,...<br><br>" +
                "<b>Không gian:</b><br>" +
                "• Rộng rãi, thoáng mát<br>" +
                "• Sức chứa: 150 khách<br><br>" +
                "<b>Chỗ để xe:</b><br>" +
                "• Xe máy: để xe ở hầm gửi xe (5.000đ/lượt)<br>"+"• Ô tô: để xe ở hầm gửi xe (10.000đ/lượt)<br><br>" +
                "<b>Điểm đặc trưng:</b><br>" +
                "• Khi thời tiết chuyển lạnh, đây là thời điểm tuyệt vời để thưởng thức BBQ thơm ngon giữa không gian thoáng đãng ngoài trời.<br>" +
                "• Đồ nướng tươi mới, giữ nóng đến tận bàn; nhân viên chuyên nghiệp, tận tình, phù hợp cho gia đình và nhóm bạn.";

        tvTomTat.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));


        MaterialButton btnBookNow = v.findViewById(R.id.btnBookNow);
        btnBookNow.setOnClickListener(view -> {
            Intent i = new Intent(getActivity(), BookingActivity.class);
            startActivity(i);
        });

        // Mặc định rút gọn (giống Quy định)
        tvTomTat.setMaxLines(12);

        btnToggle.setOnClickListener(vw -> {
            if (isExpanded) {
                tvTomTat.setMaxLines(12);
                btnToggle.setText("Xem thêm");
            } else {
                tvTomTat.setMaxLines(Integer.MAX_VALUE);
                btnToggle.setText("Thu gọn");
            }
            isExpanded = !isExpanded;

        });

        return v;
    }


}
