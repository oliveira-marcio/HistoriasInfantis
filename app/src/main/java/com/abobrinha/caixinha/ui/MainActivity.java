package com.abobrinha.caixinha.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
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

public class MainActivity extends AppCompatActivity {

    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUIElements();

        if (savedInstanceState == null) {
            if (getIntent().hasExtra(getString(R.string.shortcut_intent))) {
                PreferencesUtils.setMainHistoryCategory(this,
                        getIntent()
                                .getIntExtra(getString(R.string.shortcut_intent),
                                        PreferencesUtils.CATEGORY_HISTORIES));
            }

            if (getIntent().hasExtra(getString(R.string.fcm_extra_key))) {
                SocialUtils.openExternalLink(this, SocialUtils.WEB, getIntent()
                        .getStringExtra(getString(R.string.fcm_extra_key)));
            }

            int category = PreferencesUtils.getMainHistoryCategory(this);
            int itemId = mNavigationView.getMenu().getItem(0).getSubMenu().getItem(category).getItemId();
            mNavigationView.setCheckedItem(itemId);
            loadHistories();
        }

        HistorySyncUtils.initialize(this);

    }

    private void initializeUIElements() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final CollapsingToolbarLayout collapsingToolbarLayout =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.menu_all_histories:
                        if (!menuItem.isChecked()) {
                            PreferencesUtils.setMainHistoryCategory(MainActivity.this,
                                    PreferencesUtils.CATEGORY_HISTORIES);
                            loadHistories();
                        }
                        break;

                    case R.id.menu_favorites:
                        if (!menuItem.isChecked()) {
                            PreferencesUtils.setMainHistoryCategory(MainActivity.this,
                                    PreferencesUtils.CATEGORY_FAVORITES);
                            loadHistories();
                        }
                        break;

                    case R.id.menu_settings:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;

                    case R.id.menu_share:
                        SocialUtils.shareApp(MainActivity.this);
                        break;

                    case R.id.menu_about:
                        Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, toolbar, R.string.open_main_drawer, R.string.close_main_drawer) {
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
    }

    private void loadHistories() {
        HistoryGridFragment historyGridFragment = new HistoryGridFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frame, historyGridFragment)
                .commit();
    }
}