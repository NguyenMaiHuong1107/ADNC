package com.example.qldb;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BangGiaFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bang_gia, container, false);

        ImageView i1 = v.findViewById(R.id.img1);
        ImageView i2 = v.findViewById(R.id.img2);
        ImageView i3 = v.findViewById(R.id.img3);

        View.OnClickListener zoom = view -> {
            int resId = 0;
            if (view.getId() == R.id.img1) resId = R.drawable.ic_menu1;
            else if (view.getId() == R.id.img2) resId = R.drawable.ic_menu1;
            else if (view.getId() == R.id.img3) resId = R.drawable.ic_menu1;
            showFullImage(resId);
        };

        i1.setOnClickListener(zoom);
        i2.setOnClickListener(zoom);
        i3.setOnClickListener(zoom);
        return v;
    }

    private void showFullImage(int resId) {
        if (getContext() == null) return;
        Dialog d = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView img = new ImageView(getContext());
        img.setImageResource(resId);
        img.setAdjustViewBounds(true);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        img.setOnClickListener(v -> d.dismiss()); // chạm để đóng
        d.setContentView(img);
        d.show();
    }
}
