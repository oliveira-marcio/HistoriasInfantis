package com.abobrinha.caixinha.ui;

import android.content.Intent;
import android.database.Cursor;
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

    private HistoryFragmentAdapter mAdapter;
    private long mHistoryId;
    private int mPosition;
    private Cursor mCursor;

    ViewPager viewPager;

    private final int HISTORY_LOADER_ID = 1;

    public static final String[] MAIN_HISTORIES_PROJECTION = {
            HistoryContract.HistoriesEntry._ID,
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE
    };

    public static final int INDEX_HISTORY_ID = 0;
    public static final int INDEX_HISTORY_DATE = 1;

    private final int INVALID_ID = -1;
    private final String SELECTED_HISTORY = "selected_history";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (savedInstanceState == null) {
            mHistoryId = getIntent().getLongExtra(Intent.EXTRA_TEXT, INVALID_ID);
        } else {
            mHistoryId = savedInstanceState.getLong(SELECTED_HISTORY, INVALID_ID);
        }

        if (mHistoryId == INVALID_ID) throw
                new NullPointerException("id da história inválido.");

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        mAdapter = new HistoryFragmentAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor.moveToPosition(position)) {
                    mHistoryId = mCursor.getLong(INDEX_HISTORY_ID);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        getSupportLoaderManager().initLoader(HISTORY_LOADER_ID, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(SELECTED_HISTORY, mHistoryId);
        super.onSaveInstanceState(outState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        return new CursorLoader(this,
                HistoryContract.HistoriesEntry.CONTENT_URI,
                MAIN_HISTORIES_PROJECTION,
                null,
                null,
                HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() == 0) return;

        mCursor = data;
        mAdapter.swapCursor(data);

        int position = 0;
        data.moveToFirst();
        do {
            if (data.getLong(INDEX_HISTORY_ID) == mHistoryId) break;
            position++;
        } while (data.moveToNext());

        viewPager.setCurrentItem(position);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
