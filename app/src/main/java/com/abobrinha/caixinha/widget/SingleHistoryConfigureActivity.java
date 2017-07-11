package com.abobrinha.caixinha.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;
import com.abobrinha.caixinha.data.PreferencesUtils;
import com.abobrinha.caixinha.network.WordPressConn;
import com.abobrinha.caixinha.sync.HistorySyncUtils;


public class SingleHistoryConfigureActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener,
        SingleHistoryConfigureAdapter.OnItemClickListener {

    public static final String[] MAIN_HISTORIES_PROJECTION = {
            HistoryContract.HistoriesEntry._ID,
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE,
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_IMAGE
    };

    private RecyclerView mHistoriesList;
    private SingleHistoryConfigureAdapter mAdapter;

    private TextView mEmptyStateTextView;
    private ProgressBar mLoadingIndicator;
    private LinearLayoutManager mLayoutManager;
    private Button mOkButton;

    private long mHistorySelected;

    private int mPosition = RecyclerView.NO_POSITION;
    private final String SELECTED_KEY = "selected_position";
    private final String SELECTED_HISTORY = "selected_history";

    private final int HISTORY_LOADER_ID = 1;

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public SingleHistoryConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        setContentView(R.layout.single_history_widget_configure);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        mHistoriesList = (RecyclerView) findViewById(R.id.rv_histories);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);

        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new SingleHistoryConfigureAdapter(this, this);

        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.invalidateSelection();
                finish();
            }
        });

        mOkButton = (Button) findViewById(R.id.button_ok);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = SingleHistoryConfigureActivity.this;
                PreferencesUtils.saveWidgetHistoryPref(context, mAppWidgetId, mHistorySelected);

                Intent getHistoryDataIntent = new Intent(context, SingleHistoryIntentService.class);
                getHistoryDataIntent.setAction(SingleHistoryIntentService.ACTION_UPDATE_SINGLE_WIDGET);
                getHistoryDataIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                context.startService(getHistoryDataIntent);

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);

                mAdapter.invalidateSelection();
                finish();
            }
        });

        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
            mHistorySelected = savedInstanceState.getLong(SELECTED_HISTORY,
                    SingleHistoryConfigureAdapter.INVALID_HISTORY_ID);
            mOkButton.setEnabled(mHistorySelected != SingleHistoryConfigureAdapter.INVALID_HISTORY_ID);
        }

        showLoading();

        getSupportLoaderManager().initLoader(HISTORY_LOADER_ID, null, this);

        HistorySyncUtils.initialize(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mPosition = mLayoutManager.findFirstVisibleItemPosition();
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        outState.putLong(SELECTED_HISTORY, mHistorySelected);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_history_status_key)) &&
                sharedPreferences.getInt(key, PreferencesUtils.HISTORY_STATUS_OK) !=
                        PreferencesUtils.HISTORY_STATUS_OK) {
            showErrorMessage();
        }
    }

    private void showLoading() {
        mEmptyStateTextView.setVisibility(View.INVISIBLE);
        mHistoriesList.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        int historyStatus = PreferencesUtils.getHistoryStatus(this);
        if (historyStatus == PreferencesUtils.HISTORY_STATUS_UNKNOWN &&
                WordPressConn.isNetworkAvailable(this)) return;

        int message = R.string.empty_history_list;
        switch (historyStatus) {
            case PreferencesUtils.HISTORY_STATUS_SERVER_DOWN:
                message = R.string.empty_history_list_server_down;
                if (!WordPressConn.isNetworkAvailable(this)) {
                    message = R.string.empty_history_list_no_network;
                }
                break;
            case PreferencesUtils.HISTORY_STATUS_SERVER_INVALID:
                message = R.string.empty_history_list_server_error;
                break;
            default:
                if (!WordPressConn.isNetworkAvailable(this)) {
                    message = R.string.empty_history_list_no_network;
                }
        }

        if (mAdapter.getItemCount() > 0) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            mEmptyStateTextView.setText(message);
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            mHistoriesList.setVisibility(View.INVISIBLE);
            mOkButton.setEnabled(false);
        }
    }

    private void showHistoriesDataView() {
        mHistoriesList.setLayoutManager(mLayoutManager);
        mHistoriesList.setHasFixedSize(true);

        mHistoriesList.setAdapter(mAdapter);

        if (mPosition != RecyclerView.NO_POSITION)
            mHistoriesList.scrollToPosition(mPosition);

        mEmptyStateTextView.setVisibility(View.INVISIBLE);
        mHistoriesList.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);

        mHistorySelected = mAdapter.getSelectedHistoryId();
        mOkButton.setEnabled(mHistorySelected != SingleHistoryConfigureAdapter.INVALID_HISTORY_ID);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(this,
                HistoryContract.HistoriesEntry.CONTENT_URI,
                MAIN_HISTORIES_PROJECTION,
                null,
                null,
                HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (data != null && data.moveToFirst()) {
            showHistoriesDataView();
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(long historyId) {
        mHistorySelected = historyId;
        mOkButton.setEnabled(true);
    }
}

