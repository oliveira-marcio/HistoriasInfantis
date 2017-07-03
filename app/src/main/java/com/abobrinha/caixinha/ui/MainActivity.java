package com.abobrinha.caixinha.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.sync.HistorySyncUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HistoryGridCategoryAdapter adapter =
                new HistoryGridCategoryAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.page_margin));
        viewPager.setPageMarginDrawable(R.color.colorPrimary);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        HistorySyncUtils.initialize(this);
    }
}