package com.example.qldb.ActiVity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.qldb.Adapter.RestaurantPagerAdapter;
import com.example.qldb.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class RestaurantDetailActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        RestaurantPagerAdapter adapter = new RestaurantPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0: tab.setText("Đề xuất"); break;
                        case 1: tab.setText("Tóm tắt"); break;
                        case 2: tab.setText("Bảng giá"); break;
                        case 3: tab.setText("Quy định"); break;
                    }
                }).attach();
    }
}

