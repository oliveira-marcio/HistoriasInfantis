package com.abobrinha.caixinha.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;

public class HistoryActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private long mHistoryId;
    private int mCategory;
    private int mPosition;
    private Cursor mCursor;

    private final int HISTORY_LOADER_ID = 1;

    public static final String[] MAIN_HISTORIES_PROJECTION = {
            HistoryContract.HistoriesEntry._ID,
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE
    };

    public final Uri[] mCategoryUri = new Uri[]{
            HistoryContract.HistoriesEntry.CONTENT_URI,
            HistoryContract.HistoriesEntry.buildFavoritesUri()
    };

    public static final int INDEX_HISTORY_ID = 0;
    public static final int INDEX_HISTORY_DATE = 1;

    private final int INVALID_ID = -1;
    private final String SELECTED_HISTORY = "selected_history";
    private final String SELECTED_CATEGORY = "selected_category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (savedInstanceState == null) {
            mHistoryId = getIntent().getLongExtra(Intent.EXTRA_TEXT, INVALID_ID);
            mCategory = getIntent().getIntExtra(Intent.EXTRA_TITLE, 0);
        } else {
            mHistoryId = savedInstanceState.getLong(SELECTED_HISTORY, INVALID_ID);
            mCategory = savedInstanceState.getInt(SELECTED_CATEGORY, 0);
        }

        if (mHistoryId == INVALID_ID) throw
                new NullPointerException("id da história inválido.");

        getSupportLoaderManager().initLoader(HISTORY_LOADER_ID, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(SELECTED_HISTORY, mHistoryId);
        outState.putLong(SELECTED_CATEGORY, mCategory);
        super.onSaveInstanceState(outState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        return new CursorLoader(this,
                mCategoryUri[mCategory],
                MAIN_HISTORIES_PROJECTION,
                null,
                null,
                HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE + " DESC");
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

        loadHistory(position);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
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
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }
}
