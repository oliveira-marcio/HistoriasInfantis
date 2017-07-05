package com.abobrinha.caixinha.ui;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.sync.HistorySyncUtils;

import static com.abobrinha.caixinha.R.id.toolbar;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private final int HISTORIES_INDEX = 0;
    private final int FAVORITES_INDEX = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(toolbar);
        setSupportActionBar(mToolbar);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);
                drawerLayout.closeDrawers();

                int category = HISTORIES_INDEX;
                switch (menuItem.getItemId()) {
                    case R.id.all_histories:
                        category = HISTORIES_INDEX;
                        break;

                    case R.id.favorites:
                        category = FAVORITES_INDEX;
                        break;
                }

                loadHistories(category);
                return true;
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, mToolbar, R.string.open_main_drawer, R.string.close_main_drawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(navigationView.getMenu().getItem(HISTORIES_INDEX).getItemId());
            loadHistories(HISTORIES_INDEX);
        }

        HistorySyncUtils.initialize(this);
    }

    private void loadHistories(int category) {
        HistoryGridFragment historyGridFragment = new HistoryGridFragment();
        historyGridFragment.setCategory(category);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frame, historyGridFragment)
                .commit();
    }
}