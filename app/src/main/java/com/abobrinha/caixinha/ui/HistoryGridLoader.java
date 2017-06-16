package com.abobrinha.caixinha.ui;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.abobrinha.caixinha.data.History;
import com.abobrinha.caixinha.network.WordPressConn;
import com.abobrinha.caixinha.network.WordPressJson;

import java.util.List;

public class HistoryGridLoader extends AsyncTaskLoader<List<History>> {

    private List<History> mData;

    public HistoryGridLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        } else {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(List<History> data) {
        mData = data;
        super.deliverResult(data);
    }

    @Override
    public List<History> loadInBackground() {
        List<History> histories = null;
        String wordPressSearchResults;

        try {
            wordPressSearchResults = WordPressConn.getResponseFromAPI();
            histories = WordPressJson.getHistoriesFromJson(wordPressSearchResults);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return histories;
    }
}
