package com.abobrinha.caixinha.sync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.abobrinha.caixinha.data.HistoryContract;
import com.abobrinha.caixinha.network.WordPressConn;
import com.abobrinha.caixinha.network.WordPressJson;


public class HistorySyncTask {

    /*
     *  Essa rotina sincroniza a base de dados com a API utilizando a seguinte estratégia:
     * 1) Guardar os ID's das histórias marcadas como favoritas
     * 2) Deletar todas as histórias da base
     * 3) Recriar toda a base incluindo novas histórias
     * 4) Restaurar as marcações prévias de favoritos
     * 5) Indicar quantidade de novas histórias     */
    synchronized public static void syncHistories(Context context) {
        try {
            String wordPressSearchResults = WordPressConn.getResponseFromAPI();
            ContentValues[] historiesValues =
                    WordPressJson.getHistoriesFromJson(context, wordPressSearchResults);

            if (historiesValues != null && historiesValues.length != 0) {

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
                }
                cursor.close();

                int oldHistoryQuantity = context.getContentResolver()
                        .delete(allHistoriesUri, null, null);

                int newHistoryQuantity = context.getContentResolver()
                        .bulkInsert(allHistoriesUri, historiesValues);

                if (favoritesSaved != null) {
                    int rowsUpdated = context.getContentResolver()
                            .update(favoritesUri, null, null, favoritesSaved);
                }

                if (oldHistoryQuantity > 0 && newHistoryQuantity > oldHistoryQuantity) {
                    int newHistories = newHistoryQuantity - oldHistoryQuantity;
                    switch (newHistories) {
                        case 1:
                            // ToDo: Implementar notificação para a história nova
                            break;
                        default:
                            // ToDo: Implementar notificação indicando "x" novas histórias
                            break;
                    }
                }
            }
        } catch (Exception e) {
// ToDo: Notificar UI que houveram erros para obter dados da API
            e.printStackTrace();
        }
    }
}
