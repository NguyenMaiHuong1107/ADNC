package com.example.qldb.ActiVity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.qldb.Adapter.AdminUserAdapter;
import com.example.qldb.DatabaseHelper;
import com.example.qldb.R;
import com.example.qldb.UserModel;

import java.util.List;

public class AdminUsersFragment extends Fragment {

    private ListView lvAdminUsers;
    private DatabaseHelper dbHelper;
    private AdminUserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);

        dbHelper = new DatabaseHelper(getContext());
        lvAdminUsers = view.findViewById(R.id.lvAdminUsers);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải danh sách mỗi khi tab này được hiển thị
        loadUserList();
    }

    private void loadUserList() {
        if (getContext() == null) return;

        List<UserModel> userList = dbHelper.getAllUsers();
        adapter = new AdminUserAdapter(getContext(), userList);
        lvAdminUsers.setAdapter(adapter);
    }
}