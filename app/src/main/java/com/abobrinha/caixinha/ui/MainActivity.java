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
import android.widget.Toast;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.PreferencesUtils;
import com.abobrinha.caixinha.network.SocialUtils;
import com.abobrinha.caixinha.sync.HistorySyncUtils;

import static com.abobrinha.caixinha.R.id.toolbar;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

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
                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.menu_all_histories:
                        if (!menuItem.isChecked())
                            PreferencesUtils.setMainHistoryCategory(MainActivity.this,
                                    PreferencesUtils.HISTORIES_CATEGORY_INDEX);
                        loadHistories(PreferencesUtils.HISTORIES_CATEGORY_INDEX);
                        break;

                    case R.id.menu_favorites:
                        if (!menuItem.isChecked())
                            PreferencesUtils.setMainHistoryCategory(MainActivity.this,
                                    PreferencesUtils.FAVORITES_CATEGORY_INDEX);
                        loadHistories(PreferencesUtils.FAVORITES_CATEGORY_INDEX);
                        break;

                    case R.id.menu_share:
                        SocialUtils.shareApp(MainActivity.this);
                        break;
                    default:
                        Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                }

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
            int category = PreferencesUtils.getMainHistoryCategory(this);
            navigationView.getMenu().getItem(0).getSubMenu().getItem(category).setChecked(true);
            loadHistories(category);
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