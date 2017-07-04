package com.abobrinha.caixinha.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;
import com.abobrinha.caixinha.network.WordPressConn;
import com.abobrinha.caixinha.sync.HistorySyncTask;
import com.abobrinha.caixinha.sync.HistorySyncUtils;


public class HistoryGridFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener,
        HistoryGridAdAdapter.GridOnItemClickListener {
//ToDo: Passar linha acima para free flavor
//        HistoryGridAdapter.GridOnItemClickListener {

    public HistoryGridFragment() {
    }

    public void setCategory(int category) {
        mCategory = category;
    }

    private int mCategory;
    private final int CATEGORY_HISTORIES = 0;
    private final int CATEGORY_FAVORITES = 1;

    public final Uri[] mCategoryUri = new Uri[]{
            HistoryContract.HistoriesEntry.CONTENT_URI,
            HistoryContract.HistoriesEntry.buildFavoritesUri()
    };

    private final String SELECTED_KEY = "selected_position";
    private final String SELECTED_CATEGORY = "selected_category";

    public static final String[] MAIN_HISTORIES_PROJECTION = {
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

    private int mPosition = RecyclerView.NO_POSITION;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
            mCategory = savedInstanceState.getInt(SELECTED_CATEGORY);
        }

        View rootView = inflater.inflate(R.layout.fragment_grid_history, container, false);

        mHistoriesList = (RecyclerView) rootView.findViewById(R.id.rv_histories);
        mEmptyStateTextView = (TextView) rootView.findViewById(R.id.empty_view);
        mLoadingIndicator = (ProgressBar) rootView.findViewById(R.id.loading_indicator);

        mAdapter = new HistoryGridAdAdapter(getActivity(), this);
        //ToDo: Passar linha acima para free flavor
//        mAdapter = new HistoryGridAdapter(this, this);

        showLoading();

        getLoaderManager().initLoader(mCategory, null, this);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        outState.putInt(SELECTED_CATEGORY, mCategory);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_history_status_key)) &&
                sharedPreferences.getInt(key, HistorySyncTask.HISTORY_STATUS_OK) !=
                        HistorySyncTask.HISTORY_STATUS_OK) {
            showErrorMessage();
        }
        // ToDo: Avaliar se deve ser exibido uma mensagem em caso de sincronia com sucesso.
    }

    private void showLoading() {
        mEmptyStateTextView.setVisibility(View.INVISIBLE);
        mHistoriesList.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        int historyStatus = getHistoryStatus();
        if (historyStatus == HistorySyncTask.HISTORY_STATUS_UNKNOWN &&
                WordPressConn.isNetworkAvailable(getActivity())) return;

        int message = R.string.empty_history_list;
        switch (historyStatus) {
            case HistorySyncTask.HISTORY_STATUS_SERVER_DOWN:
                message = R.string.empty_history_list_server_down;
                if (!WordPressConn.isNetworkAvailable(getActivity())) {
                    message = R.string.empty_history_list_no_network;
                }
                break;
            case HistorySyncTask.HISTORY_STATUS_SERVER_INVALID:
                message = R.string.empty_history_list_server_error;
                break;
            default:
                if (!WordPressConn.isNetworkAvailable(getActivity())) {
                    message = R.string.empty_history_list_no_network;
                }
        }

        if (mAdapter.getItemCount() > 0) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        } else {
            mEmptyStateTextView.setText(message);
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            mHistoriesList.setVisibility(View.INVISIBLE);
        }
    }

    private int getHistoryStatus() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return sp.getInt(getString(R.string.pref_history_status_key),
                HistorySyncTask.HISTORY_STATUS_UNKNOWN);
    }

    private void showHistoriesDataView(Cursor data) {
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 1);

        mHistoriesList.setLayoutManager(layoutManager);
        mHistoriesList.setHasFixedSize(true);

        mHistoriesList.setAdapter(mAdapter);

        if (mCategory == CATEGORY_FAVORITES) {
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                      RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    long historyId = mAdapter.getHistoryIdAtPosition(viewHolder.getAdapterPosition());
                    if (historyId == -1) return;

                    ContentValues values = new ContentValues();
                    values.put(HistoryContract.HistoriesEntry.COLUMN_FAVORITE,
                            HistoryContract.IS_NOT_FAVORITE);

                    getActivity().getContentResolver().update(
                            HistoryContract.HistoriesEntry.buildSingleHistoryUri(historyId),
                            values,
                            null,
                            null);
                }

                @Override
                public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    int position = viewHolder.getAdapterPosition();
                    if (position >= 0 && mAdapter.getHistoryIdAtPosition(position) == -1)
                        return 0;
                    return super.getSwipeDirs(recyclerView, viewHolder);
                }

                @Override
                public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                        int actionState, boolean isCurrentlyActive) {
                    Bitmap icon;
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        View itemView = viewHolder.itemView;
                        float height = (float) itemView.getBottom() - (float) itemView.getTop();
                        float width = height / 3;

                        if (dX <= 0) {
                            Paint p = new Paint();
                            p.setColor(ActivityCompat.getColor(getActivity(),
                                    R.color.colorSwipeBackground));
                            RectF background = new RectF(
                                    (float) itemView.getRight() + dX,
                                    (float) itemView.getTop(),
                                    (float) itemView.getRight(),
                                    (float) itemView.getBottom());
                            c.drawRect(background, p);
                            c.clipRect(background);
                            icon = BitmapFactory.decodeResource(getResources(),
                                    R.drawable.ic_delete_white);
                            RectF icon_dest = new RectF(
                                    (float) itemView.getRight() - 2 * width,
                                    (float) itemView.getTop() + width,
                                    (float) itemView.getRight() - width,
                                    (float) itemView.getBottom() - width);
                            c.drawBitmap(icon, null, icon_dest, p);
                            c.restore();
                        }
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState,
                            isCurrentlyActive);
                }
            }).attachToRecyclerView(mHistoriesList);
        }

        if (mPosition != RecyclerView.NO_POSITION)
            mHistoriesList.smoothScrollToPosition(mPosition);

        mEmptyStateTextView.setVisibility(View.INVISIBLE);
        mHistoriesList.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                mCategoryUri[mCategory],
                MAIN_HISTORIES_PROJECTION,
                null,
                null,
                HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (data != null && data.moveToFirst()) {
            showHistoriesDataView(data);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(long historyId, int position) {
        mPosition = position;
        Intent intent = new Intent(getActivity(), HistoryActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, historyId);
        intent.putExtra(Intent.EXTRA_TITLE, mCategory);
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mCategory == CATEGORY_HISTORIES) {
            inflater.inflate(R.menu.main, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            HistorySyncUtils.startImmediateSync(getActivity());
            if (mAdapter.getItemCount() == 0) showLoading();
            // ToDo: Na versão final, substituir toast por progressbar circular na appbar
            Toast.makeText(getActivity(), "Atualizando...", Toast.LENGTH_SHORT).show();
            return true;
        }

        // Métodos para teste de sincronização e notificação.
        // Deve ser deletado na versão final
        // ToDo: Remover código abaixo até o return super e remover entradas do menu
        Long lastId = null;
        Long beforeLastId = null;

        Uri uri = HistoryContract.HistoriesEntry.CONTENT_URI;
        Cursor cursor = getActivity().getContentResolver().query(uri,
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
            getActivity().getContentResolver().delete(uri, HistoryContract.HistoriesEntry._ID +
                    "=" + lastId, null);
            if (beforeLastId != null) {
                getActivity().getContentResolver().delete(uri, HistoryContract.HistoriesEntry._ID +
                        "=" + beforeLastId, null);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

// ToDo: Verificar se Gson ainda será utilizado e removê-lo.