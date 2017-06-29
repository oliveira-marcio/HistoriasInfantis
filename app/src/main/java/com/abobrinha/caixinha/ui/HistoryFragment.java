package com.abobrinha.caixinha.ui;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;

import org.jsoup.Jsoup;


public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public HistoryFragment() {
    }

    private Uri mHistoryUri;
    private Uri mParagraphsUri;
    private int mPosition = RecyclerView.NO_POSITION;

    private final String VISIBLE_POSITION = "visible_position";

    private RecyclerView mHistoryView;
    private TextView mTitleTextView;
    private LinearLayoutManager mLayoutManager;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        if (mHistoryUri == null) throw
                new NullPointerException("URI para HistoryFragment não pode ser nula.");

        long historyId = ContentUris.parseId(mHistoryUri);
        mParagraphsUri = HistoryContract.ParagraphsEntry.buildParagraphsFromHistoryId(historyId);

        mLoadingIndicator = (ProgressBar) rootView.findViewById(R.id.loading_indicator);

        mTitleTextView = (TextView) rootView.findViewById(R.id.title);
        mHistoryView = (RecyclerView) rootView.findViewById(R.id.rv_history);
        mLayoutManager = new LinearLayoutManager(getActivity());

        mHistoryView.setLayoutManager(mLayoutManager);
        mHistoryView.setHasFixedSize(true);

        mAdapter = new HistoryAdapter(getActivity());
        mHistoryView.setAdapter(mAdapter);

        showLoading();

        getLoaderManager().initLoader(HISTORY_LOADER_ID, null, this);
        getLoaderManager().initLoader(PARAGRAPH_LOADER_ID, null, this);

        if (savedInstanceState != null && savedInstanceState.containsKey(VISIBLE_POSITION)) {
            mPosition = savedInstanceState.getInt(VISIBLE_POSITION);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mPosition = mLayoutManager.findFirstVisibleItemPosition();
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(VISIBLE_POSITION, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    public void setHistoryUri(Uri uri) {
        mHistoryUri = uri;
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
                return new CursorLoader(getActivity(),
                        mHistoryUri,
                        MAIN_HISTORY_PROJECTION,
                        null,
                        null,
                        null);

            case PARAGRAPH_LOADER_ID:
                return new CursorLoader(getActivity(),
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
                mTitleTextView.setText(Jsoup.parse(data.getString(INDEX_HISTORY_TITLE)).text());
                break;

            case PARAGRAPH_LOADER_ID:
                mAdapter.swapCursor(data);
                if (mPosition != RecyclerView.NO_POSITION)
                    mHistoryView.scrollToPosition(mPosition);
                showHistoryDataView();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}