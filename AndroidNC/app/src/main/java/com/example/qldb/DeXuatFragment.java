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

public class DeXuatFragment extends Fragment {
    private boolean isExpanded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_de_xuat, container, false);

        TextView tv = v.findViewById(R.id.tvDeXuat);
        TextView btn = v.findViewById(R.id.btnXemThemDeXuat);

        String html =
                "<b>Đề xuất</b><br><br>" +
                        "<b>*Ưu đãi tặng kèm:*</b> Chương trình ưu đãi đang được cập nhật.<br><br>" +
                        "<b>Tóm tắt Vườn nướng BBQ - S101 Vinhomes Smart City</b><br><br>" +
                        "<b>Phù hợp:</b><br>" +
                        "• Ăn gia đình, tụ tập bạn bè, liên hoan văn phòng, họp nhóm, tổ chức sinh nhật, tiệc công ty, tiếp khách, tiệc thôi nôi, kỷ niệm ngày đặc biệt,...<br><br>" +
                        "<b>Món đặc sắc:</b><br>" +
                        "• Tôm sú nướng, nầm nướng sate, má heo mềm, ba chỉ bò cuốn nấm, ba chỉ heo Hàn, cánh gà BBQ, hàu nướng mỡ hành,...<br><br>" +
                        "<b>Không gian:</b><br>" +
                        "• Rộng rãi, thoáng mát<br>" +
                        "• Sức chứa: 150 khách<br><br>" +
                        "<b>Chỗ để xe:</b><br>" +
                        "• Xe máy: để xe ở hầm gửi xe (5.000đ/lượt)<br>"+
                        "• Ô tô: để xe ở hầm gửi xe (10.000đ/lượt)"
                ;

        tv.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));

        // Mặc định hiển thị rút gọn
        tv.setMaxLines(12);
        MaterialButton btnBookNow = v.findViewById(R.id.btnBookNow);
        btnBookNow.setOnClickListener(view -> {
            Intent i = new Intent(getActivity(), BookingActivity.class);
            startActivity(i);
        });

        btn.setOnClickListener(view -> {
            if (isExpanded) {
                tv.setMaxLines(12);
                btn.setText("Xem thêm");
            } else {
                tv.setMaxLines(Integer.MAX_VALUE);
                btn.setText("Thu gọn");
            }
            isExpanded = !isExpanded;
        });

        return v;
    }
}
