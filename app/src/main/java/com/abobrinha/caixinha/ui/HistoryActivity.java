package com.abobrinha.caixinha.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;
import com.abobrinha.caixinha.data.PreferencesUtils;

public class HistoryActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private long mHistoryId;
    private int mCategory;
    private int mPosition;
    private Cursor mCursor;

    private final int HISTORY_LOADER_ID = 1;

    public static final String[] MAIN_HISTORIES_PROJECTION = {
            HistoryContract.HistoriesEntry._ID,
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE,
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE
    };

    public final Uri[] mCategoryUri = new Uri[]{
            HistoryContract.HistoriesEntry.CONTENT_URI,
            HistoryContract.HistoriesEntry.buildFavoritesUri()
    };

    public final int[] mCategoryIcon = new int[]{
            R.drawable.ic_about,
            R.drawable.ic_favorite
    };

    public static final int INDEX_HISTORY_ID = 0;
    public static final int INDEX_HISTORY_TITLE = 1;
    public static final int INDEX_HISTORY_DATE = 2;

    private final int INVALID_ID = -1;
    private final String SELECTED_HISTORY = "selected_history";

    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_history);

        if (savedInstanceState == null) {
            mHistoryId = getIntent().getLongExtra(Intent.EXTRA_TEXT, INVALID_ID);
            // Se esta Activity foi aberta por uma notificação e a última categoria aberta pelo
            // usuário foi a de favoritos, é necessário setar a categoria para "Todas", pois a
            // nova história ainda não consta nos favoritos do usuário.
            int categoryByNotification = getIntent().
                    getIntExtra(getString(R.string.notification_intent),
                            PreferencesUtils.getMainHistoryCategory(this));
            PreferencesUtils.setMainHistoryCategory(this, categoryByNotification);

        } else {
            mHistoryId = savedInstanceState.getLong(SELECTED_HISTORY, INVALID_ID);
        }

        // ToDo: Widget vai precisar alterar sharedpreference sempre que clicar numa história.
        mCategory = PreferencesUtils.getMainHistoryCategory(this);

        if (mHistoryId == INVALID_ID) throw
                new NullPointerException("id da história inválido.");

        ImageView upButton = (ImageView) findViewById(R.id.action_up);

        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getSupportLoaderManager().initLoader(HISTORY_LOADER_ID, null, this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(SELECTED_HISTORY, mHistoryId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        return new CursorLoader(this,
                mCategoryUri[mCategory],
                MAIN_HISTORIES_PROJECTION,
                null,
                null,
                PreferencesUtils.getGridHistoryOrder(this));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.moveToFirst()) {
            finish();
            return;
        }

        mCursor = data;

        boolean favoriteRemoved = true;
        int position = -1;
        data.moveToFirst();
        do {
            position++;
            if (data.getLong(INDEX_HISTORY_ID) == mHistoryId) {
                favoriteRemoved = false;
                break;
            }
        } while (data.moveToNext());

        if (favoriteRemoved) {
            if (mPosition == data.getCount()) mPosition--;
            position = mPosition;
        }

        loadDrawer(position);
        loadHistory(position);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void loadDrawer(int initialPosition) {
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (mCursor.moveToFirst()) {
            SubMenu menu = mNavigationView.getMenu().getItem(0).getSubMenu();
            menu.clear();

            int i = 0;
            do {
                MenuItem item = menu.add(R.id.history_group, i, i, mCursor.getString(INDEX_HISTORY_TITLE));
                item.setIcon(mCategoryIcon[mCategory]);
                i++;
            } while (mCursor.moveToNext());

            menu.setGroupCheckable(R.id.history_group, true, true);

            int id = mNavigationView.getMenu().getItem(0).getSubMenu().getItem(initialPosition).getItemId();
            mNavigationView.setCheckedItem(id);

            mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    drawerLayout.closeDrawers();
                    if (menuItem.getItemId() == R.id.menu_settings) {
                        startActivity(new Intent(HistoryActivity.this, SettingsActivity.class));
                    } else {
                        loadHistory(menuItem.getItemId());
                    }
                    return true;
                }
            });
        }
    }

    private void loadHistory(int position) {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        HistoryFragmentAdapter adapter = new HistoryFragmentAdapter(getSupportFragmentManager(), mCursor);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor.moveToPosition(position)) {
                    mPosition = position;
                    mHistoryId = mCursor.getLong(INDEX_HISTORY_ID);
                    int id = mNavigationView.getMenu().getItem(0).getSubMenu().getItem(position).getItemId();
                    mNavigationView.setCheckedItem(id);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_order_key))) {
            getSupportLoaderManager().restartLoader(HISTORY_LOADER_ID, null, this);
        }
    }
}
