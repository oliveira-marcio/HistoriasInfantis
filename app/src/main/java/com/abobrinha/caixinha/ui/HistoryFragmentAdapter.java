package com.abobrinha.caixinha.ui;

import android.database.Cursor;
import android.net.Uri;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.abobrinha.caixinha.data.HistoryContract;


public class HistoryFragmentAdapter extends FragmentStatePagerAdapter {
    private Cursor mCursor;

    public HistoryFragmentAdapter(FragmentManager fm, Cursor cursor) {
        super(fm);
        mCursor = cursor;
    }

    @Override
    public Fragment getItem(int position) {
        mCursor.moveToPosition(position);
        long historyId = mCursor.getLong(0);
        Uri historyUri = HistoryContract.HistoriesEntry.buildSingleHistoryUri(historyId);

        HistoryFragment historyFragment = new HistoryFragment();
        historyFragment.setHistoryUri(historyUri);
        return historyFragment;
    }

    @Override
    public int getCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }
}
