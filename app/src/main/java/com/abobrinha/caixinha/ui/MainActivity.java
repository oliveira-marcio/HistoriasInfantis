package com.abobrinha.caixinha.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;
import com.abobrinha.caixinha.network.WordPressConn;
import com.abobrinha.caixinha.sync.HistorySyncTask;
import com.abobrinha.caixinha.sync.HistorySyncUtils;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        HistoryGridAdAdapter.GridOnItemClickListener {
//ToDo: Passar linha acima para free flavor
//        HistoryGridAdapter.GridOnItemClickListener {

    private static final String SELECTED_KEY = "selected_position";
    private final int HISTORY_LOADER_ID = 1;

    public final String[] MAIN_HISTORIES_PROJECTION = {
            HistoryContract.HistoriesEntry._ID,
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE,
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_IMAGE,
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE
    };

    public static final int INDEX_HISTORY_ID = 0;
    public static final int INDEX_HISTORY_TITLE = 1;
    public static final int INDEX_HISTORY_IMAGE = 2;
    public static final int INDEX_HISTORY_DATE = 3;

    private RecyclerView mHistoriesList;
    private HistoryGridAdAdapter mAdapter;
    //ToDo: Passar linha acima para free flavor
//    private HistoryGridAdapter mAdapter;

    private TextView mEmptyStateTextView;
    private ProgressBar mLoadingIndicator;
    // ToDo: implementar refresh

    private int mPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);

        mHistoriesList = (RecyclerView) findViewById(R.id.rv_histories);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);

        mHistoriesList.setLayoutManager(layoutManager);
        mHistoriesList.setHasFixedSize(true);

        mAdapter = new HistoryGridAdAdapter(this, this);
        //ToDo: Passar linha acima para free flavor
//        mAdapter = new HistoryGridAdapter(this, this);
        mHistoriesList.setAdapter(mAdapter);

        showLoading();

        getSupportLoaderManager().initLoader(HISTORY_LOADER_ID, null, this);
        HistorySyncUtils.initialize(this);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    private void showLoading() {
        mHistoriesList.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showHistoriesDataView() {
        mEmptyStateTextView.setVisibility(View.INVISIBLE);
        mHistoriesList.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage(int errorId) {
        mEmptyStateTextView.setText(errorId);
        mEmptyStateTextView.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mHistoriesList.setVisibility(View.INVISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                HistoryContract.HistoriesEntry.CONTENT_URI,
                MAIN_HISTORIES_PROJECTION,
                null,
                null,
                HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (mPosition != RecyclerView.NO_POSITION)
            mHistoriesList.smoothScrollToPosition(mPosition);

        if (data != null && data.moveToFirst()) {
            showHistoriesDataView();
        } else {
            if (!WordPressConn.isNetworkAvailable(this)) {
                showErrorMessage(R.string.empty_history_list_no_network);
            } else {
                showErrorMessage(R.string.empty_history_list);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(long historyId, int position) {
        mPosition = position;
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.setData(HistoryContract.HistoriesEntry.buildSingleHistoryUri(historyId));
        startActivity(intent);
    }

    private int getHistoryStatus(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getInt(getString(R.string.pref_history_status_key),
                HistorySyncTask.HISTORY_STATUS_UNKNOWN);
    }

    // Métodos para teste de sincronização e notificação.
    // Deve ser deletado na versão final
    // ToDo: REMOVER MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.teste, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        Long lastId = null;
        Long beforeLastId = null;

        Uri uri = HistoryContract.HistoriesEntry.CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri,
                MAIN_HISTORIES_PROJECTION,
                null,
                null,
                HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            lastId = cursor.getLong(INDEX_HISTORY_ID);

            if (id == R.id.action_delete_two && cursor.moveToNext()) {
                beforeLastId = cursor.getLong(INDEX_HISTORY_ID);
            }
        }
        cursor.close();

        if (lastId != null) {
            getContentResolver().delete(uri, HistoryContract.HistoriesEntry._ID + "=" + lastId, null);
            if (beforeLastId != null) {
                getContentResolver().delete(uri, HistoryContract.HistoriesEntry._ID + "=" + beforeLastId, null);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

// ToDo: Verificar se Gson ainda será utilizado e removê-lo.