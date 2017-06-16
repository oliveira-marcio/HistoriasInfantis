package com.abobrinha.caixinha.ui;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.History;
import com.abobrinha.caixinha.network.WordPressConn;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<History>>,
        HistoryGridAdAdapter.GridOnItemClickListener {
//ToDo: Passar linha acima para free flavor
//        HistoryGridAdapter.GridOnItemClickListener {

    private static final int HISTORY_LOADER_ID = 1;

    private RecyclerView mHistoriesList;
    private HistoryGridAdAdapter mAdapter;
    //ToDo: Passar linha acima para free flavor
//    private HistoryGridAdapter mAdapter;
    private List<History> mHistoriesData = new ArrayList<>();
    // ToDo: implementar progressbar
    // ToDo: implementar refresh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHistoriesList = (RecyclerView) findViewById(R.id.rv_histories);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);

        mHistoriesList.setLayoutManager(layoutManager);
        mHistoriesList.setHasFixedSize(true);

        mAdapter = new HistoryGridAdAdapter(mHistoriesData, this);
        //ToDo: Passar linha acima para free flavor
//        mAdapter = new HistoryGridAdapter(mHistoriesData, this);
        mHistoriesList.setAdapter(mAdapter);

        getLoaderManager().initLoader(HISTORY_LOADER_ID, null, this);
    }

    @Override
    public Loader<List<History>> onCreateLoader(int id, Bundle args) {
        return new HistoryGridLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<History>> loader, List<History> histories) {
        mAdapter.clear();

        if (histories != null) {
// ToDo: exibir lista
//            showMoviesDataView();
            mHistoriesData = histories;
            Log.v("Main.getItemCount()", "" + mHistoriesData.size());
            mAdapter.addAll(mHistoriesData);
        } else {
// ToDo: exibir erro
//            showErrorMessage(R.string.no_movies);
            if(!WordPressConn.isNetworkAvailable(this)){
                Toast.makeText(this, "SEM INTERNET", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "DEU RUIM COM AS HISTÓRIAS", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<History>> loader) {
        mAdapter.clear();
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Gson gson = new Gson();
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, gson.toJson(mHistoriesData.get(clickedItemIndex)));
        startActivity(intent);
    }
}
