package com.abobrinha.caixinha.ui;

import android.app.LoaderManager;
import android.content.Loader;
import android.graphics.Movie;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.History;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<History>>,
        HistoryAdapter.HistoryAdapterOnItemClickListener {

    private static final int HISTORY_LOADER_ID = 1;

    private RecyclerView mHistoriesList;
    private HistoryAdapter mAdapter;
    private List<History> mHistoriesData = new ArrayList<>();
    // ToDo: implementar progressbar
    // ToDo: implementar refresh
    // ToDo: implementar teste de conexão

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHistoriesList = (RecyclerView) findViewById(R.id.rv_histories);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);

        mHistoriesList.setLayoutManager(layoutManager);
        mHistoriesList.setHasFixedSize(true);

        mAdapter = new HistoryAdapter(mHistoriesData, this);
        mHistoriesList.setAdapter(mAdapter);

        getLoaderManager().initLoader(HISTORY_LOADER_ID, null, this);
    }

    @Override
    public Loader<List<History>> onCreateLoader(int id, Bundle args) {
        return new HistoryLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<History>> loader, List<History> histories) {
        mAdapter.clear();

        if (histories != null) {
// ToDo: exibir lista
//            showMoviesDataView();
            mHistoriesData = histories;
            mAdapter.addAll(mHistoriesData);
        } else {
// ToDo: exibir erro
//            showErrorMessage(R.string.no_movies);
            Toast.makeText(this, "DEU RUIM COM AS HISTÓRIAS", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<History>> loader) {
        mAdapter.clear();
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Toast.makeText(this, "" + clickedItemIndex, Toast.LENGTH_SHORT).show();
    }
}
