package com.abobrinha.caixinha.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.abobrinha.caixinha.R;


public class HistoryGridCategoryAdapter extends FragmentPagerAdapter {

    // Array com os títulos de todas as abas para o TabLayout de ordenação dos filmes.
    private String[] mCategoryLabels;
    private Context mContext;

    public HistoryGridCategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        mCategoryLabels = mContext.getResources().getStringArray(R.array.category_labels);
    }

    @Override
    public Fragment getItem(int position) {
        HistoryGridFragment historyGridFragment = new HistoryGridFragment();
        historyGridFragment.setCategory(position);
        return historyGridFragment;
    }

    @Override
    public int getCount() {
        return mCategoryLabels.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mCategoryLabels[position];
    }
}