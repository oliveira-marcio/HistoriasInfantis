package com.abobrinha.caixinha.ui;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;

public class HistoryActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mHistoryUri;
    private Uri mParagraphsUri;

    private RecyclerView mHistoryView;
    private HistoryAdapter mAdapter;
    private ProgressBar mLoadingIndicator;

    private final int HISTORY_LOADER_ID = 1;
    private final int PARAGRAPH_LOADER_ID = 2;

    public final String[] MAIN_HISTORY_PROJECTION = {
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE,
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_IMAGE
    };

    public static final int INDEX_HISTORY_TITLE = 0;
    public static final int INDEX_HISTORY_IMAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mHistoryUri = getIntent().getData();
        if (mHistoryUri == null) throw
                new NullPointerException("URI para HistoryActivity não pode ser nula.");

        long historyId = ContentUris.parseId(mHistoryUri);
        mParagraphsUri = HistoryContract.ParagraphsEntry.buildParagraphsFromHistoryId(historyId);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);

        mHistoryView = (RecyclerView) findViewById(R.id.rv_history);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mHistoryView.setLayoutManager(layoutManager);
        mHistoryView.setHasFixedSize(true);

        mAdapter = new HistoryAdapter(this);
        mHistoryView.setAdapter(mAdapter);

        showLoading();

        getSupportLoaderManager().initLoader(HISTORY_LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(PARAGRAPH_LOADER_ID, null, this);
    }

    private void showLoading() {
        mHistoryView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showHistoryDataView() {
        mHistoryView.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case HISTORY_LOADER_ID:
                return new CursorLoader(this,
                        mHistoryUri,
                        MAIN_HISTORY_PROJECTION,
                        null,
                        null,
                        null);

            case PARAGRAPH_LOADER_ID:
                return new CursorLoader(this,
                        mParagraphsUri,
                        null,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader não implementado: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.moveToFirst()) throw
                new NullPointerException("URI para HistoryActivity não pode ser nula.");

        switch (loader.getId()) {
            case HISTORY_LOADER_ID:
                setTitle(data.getString(INDEX_HISTORY_TITLE));
                break;

            case PARAGRAPH_LOADER_ID:
                mAdapter.swapCursor(data);
                showHistoryDataView();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
