package com.abobrinha.caixinha.sync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.abobrinha.caixinha.data.HistoryContract;
import com.abobrinha.caixinha.data.PreferencesUtils;
import com.abobrinha.caixinha.network.WordPressConn;

import org.json.JSONException;

import java.io.IOException;


public class HistorySyncTask {

    /*
     *  Essa rotina sincroniza a base de dados com a API utilizando a seguinte estratégia:
     * 1) Guardar os ID's das histórias marcadas como favoritas
     * 2) Deletar todas as histórias da base
     * 3) Recriar toda a base incluindo novas histórias
     * 4) Restaurar as marcações prévias de favoritos
     * 5) Indicar quantidade de novas histórias
     *
     * Todo: Idéias para melhor sincronia e otimizar tráfego de dados:
     * 1) Inicialmente, buscar apenas os ID's e datas de criação e modificação na API
     * https://public-api.wordpress.com/rest/v1.1/sites/113100833/posts?category=historias-infantis-abobrinha&number=100&fields=ID,date,modified
     *
     * 2) Comparar com a base e gerar lista dos ID's que estão diferentes ou ausentes e ID's que estão a mais na base (foram deletados na fonte)
     *
     * 3) Deletar ID's a mais da base
     *
     * 4) Buscar dados completos de cada ID ausente ou modificado:
     * https://public-api.wordpress.com/rest/v1.1/sites/113100833/posts/2436?fields=ID,date,modified,title,URL,featured_image,content
     *
     * 5) Inserir dados ausentes e atualizar os modificados
     */
    synchronized public static void syncHistories(Context context) {
        try {
            PreferencesUtils.setHistoryStatus(context, PreferencesUtils.HISTORY_STATUS_UNKNOWN);

            ContentValues[] historiesValues = WordPressConn.getDataFromAllApiPages(context);
            if (historiesValues == null) {
                PreferencesUtils.setHistoryStatus(context, PreferencesUtils.HISTORY_STATUS_SERVER_DOWN);
                return;
            }

            Uri allHistoriesUri = HistoryContract.HistoriesEntry.CONTENT_URI;
            Uri favoritesUri = HistoryContract.HistoriesEntry.buildFavoritesUri();

            String[] favoritesSaved = null;

            String[] projection = new String[]{HistoryContract.HistoriesEntry._ID};
            Cursor cursor = context.getContentResolver()
                    .query(favoritesUri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int i = 0;
                favoritesSaved = new String[cursor.getCount()];

                do {
                    favoritesSaved[i++] = cursor.getString(0);
                } while (cursor.moveToNext());

                cursor.close();
            }

            int oldHistoryQuantity = context.getContentResolver()
                    .delete(allHistoriesUri, null, null);

            int newHistoryQuantity = context.getContentResolver()
                    .bulkInsert(allHistoriesUri, historiesValues);

            if (favoritesSaved != null) {
                context.getContentResolver().update(favoritesUri, null, null, favoritesSaved);
            }

            boolean notificationsEnabled = PreferencesUtils.areNotificationsEnabled(context);

            if (notificationsEnabled && oldHistoryQuantity > 0 && newHistoryQuantity > oldHistoryQuantity) {
                int newHistories = newHistoryQuantity - oldHistoryQuantity;
                NotificationUtils.notifyUserOfNewHistories(context, newHistories);
            }

            PreferencesUtils.setHistoryStatus(context, PreferencesUtils.HISTORY_STATUS_OK);

            NotificationUtils.updateWidgets(context);

        } catch (IOException e) {
            PreferencesUtils.setHistoryStatus(context, PreferencesUtils.HISTORY_STATUS_SERVER_DOWN);
        } catch (JSONException e) {
            PreferencesUtils.setHistoryStatus(context, PreferencesUtils.HISTORY_STATUS_SERVER_INVALID);
        }
    }
}
