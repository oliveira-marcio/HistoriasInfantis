package com.abobrinha.caixinha.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.sync.HistorySyncUtils;

public class MainActivity extends AppCompatActivity {
//ToDo: Passar linha acima para free flavor
//        HistoryGridAdapter.GridOnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HistoryGridCategoryAdapter adapter =
                new HistoryGridCategoryAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.page_margin));
        viewPager.setPageMarginDrawable(R.color.colorPrimary);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        HistorySyncUtils.initialize(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    // ToDo: Restaurar menu
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        int id = item.getItemId();
//        if (id == R.id.action_refresh) {
//            HistorySyncUtils.startImmediateSync(this);
//            if (mAdapter.getItemCount() == 0) showLoading();
//            // ToDo: Na versão final, substituir toast por progressbar circular na appbar
//            Toast.makeText(this, "Atualizando...", Toast.LENGTH_SHORT).show();
//            return true;
//        }
//
//        // Métodos para teste de sincronização e notificação.
//        // Deve ser deletado na versão final
//        // ToDo: Remover código abaixo até o return super e remover entradas do menu
//        Long lastId = null;
//        Long beforeLastId = null;
//
//        Uri uri = HistoryContract.HistoriesEntry.CONTENT_URI;
//        Cursor cursor = getContentResolver().query(uri,
//                MAIN_HISTORIES_PROJECTION,
//                null,
//                null,
//                HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE + " DESC");
//
//        if (cursor != null && cursor.moveToFirst()) {
//            lastId = cursor.getLong(INDEX_HISTORY_ID);
//
//            if (id == R.id.action_delete_two && cursor.moveToNext()) {
//                beforeLastId = cursor.getLong(INDEX_HISTORY_ID);
//            }
//        }
//        cursor.close();
//
//        if (lastId != null) {
//            getContentResolver().delete(uri, HistoryContract.HistoriesEntry._ID + "=" + lastId, null);
//            if (beforeLastId != null) {
//                getContentResolver().delete(uri, HistoryContract.HistoriesEntry._ID + "=" + beforeLastId, null);
//            }
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}

// ToDo: Verificar se Gson ainda será utilizado e removê-lo.