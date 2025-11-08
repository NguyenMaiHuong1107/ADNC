package com.example.qldb.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.qldb.R;
import com.example.qldb.UserModel;

import java.util.List;

public class AdminUserAdapter extends ArrayAdapter<UserModel> {

    public AdminUserAdapter(@NonNull Context context, @NonNull List<UserModel> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.admin_user_card_item, parent, false);
        }

        UserModel currentUser = getItem(position);

        TextView tvAvatar = listItemView.findViewById(R.id.tvUserAvatar);
        TextView tvName = listItemView.findViewById(R.id.tvUserName);
        TextView tvPhone = listItemView.findViewById(R.id.tvUserPhone);

        if (currentUser != null) {
            tvName.setText(currentUser.fullName);
            tvPhone.setText(currentUser.phone);

            // Đặt chữ cái đầu tiên của tên làm Avatar
            if (currentUser.fullName != null && !currentUser.fullName.isEmpty()) {
                tvAvatar.setText(currentUser.fullName.substring(0, 1).toUpperCase());
            } else {
                tvAvatar.setText("?");
            }
        }

        return listItemView;
    }
}