package com.abobrinha.caixinha.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import com.abobrinha.caixinha.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TestHistoryProvider {
    private final Context context = InstrumentationRegistry.getTargetContext();

    @Before
    public void setUp() {
        deleteAllRecordsFromHistoriesTable();
    }

    /*
     * Este método testa as 4 possíveis combinações de URI do Provider
     */
    @Test
    public void testUriMatcher() {
        UriMatcher matcher = HistoryProvider.buildUriMatcher();

        Uri uri = HistoryContract.HistoriesEntry.CONTENT_URI;
        String uriError = "Erro no parsing da Uri de histórias.";
        assertEquals(uriError, HistoryProvider.CODE_HISTORIES, matcher.match(uri));

        uri = HistoryContract.HistoriesEntry.buildFavoritesUri();
        uriError = "Erro no parsing da Uri de histórias favoritas.";
        assertEquals(uriError, HistoryProvider.CODE_FAVORITES_HISTORIES, matcher.match(uri));

        uri = HistoryContract.HistoriesEntry.buildSingleHistoryUri(0);
        uriError = "Erro no parsing da Uri de uma única história.";
        assertEquals(uriError, HistoryProvider.CODE_SINGLE_HISTORY, matcher.match(uri));

        uri = HistoryContract.ParagraphsEntry.buildParagraphsFromHistoryUri(0);
        uriError = "Erro no parsing da Uri de parágrafos de uma história.";
        assertEquals(uriError, HistoryProvider.CODE_PARAGRAPHS, matcher.match(uri));
    }

    /*
     *  Este teste valida os seguintes métodos do provider:
     *
     *  1) bulkInsert, usando a URI de histórias (CODE_HISTORIES, do matcher)
     *  2) query, usando a URI de histórias (CODE_HISTORIES, do matcher)
     *  2) query, usando a URI de histórias individuais (CODE_SINGLE_HISTORY, do matcher)
     *  3) query, usando a URI de parágrafos (CODE_PARAGRAPHS, do matcher)
     */
    @Test
    public void testHistoriesBulkInsert() {
        Uri uri = HistoryContract.HistoriesEntry.CONTENT_URI;
        ContentValues[] historyValues =
                TestDbUtilities.createBulkInsertTestHistoryContentValues(context,
                        TestDbUtilities.CONTENT_VALUES_HIGHER_QUANTITY, true);

        int rowsInserted = context.getContentResolver().bulkInsert(uri, historyValues);

        String bulkinsertFailed = "Houveram falhas para inserir as histórias no database.";
        assertEquals(bulkinsertFailed, historyValues.length, rowsInserted);

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta de histórias.";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        int i = 0;
        do {
            String expectedResultDidntMatchActual =
                    "Valores de histórias atuais não batem com o esperado.";
            TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                    cursor,
                    historyValues[i++]);
        } while (cursor.moveToNext());

        cursor.close();

        for (i = 0; i < historyValues.length; i++) {
            uri = HistoryContract.HistoriesEntry.buildSingleHistoryUri(i);
            cursor = context.getContentResolver().query(uri, null, null, null, null);

            emptyQueryError = "Erro: Nenhuma história retornada para o id " + i + ".";
            assertTrue(emptyQueryError,
                    cursor.moveToFirst());

            String expectedResultDidntMatchActual =
                    "Valores atuais para a história com id " + i + " não batem com o esperado.";
            TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                    cursor,
                    historyValues[i]);

            cursor.close();
        }

        historyValues =
                TestDbUtilities.createBulkInsertTestHistoryContentValues(context,
                        TestDbUtilities.CONTENT_VALUES_HIGHER_QUANTITY, true);

        for (i = 0; i < historyValues.length; i++) {
            uri = HistoryContract.ParagraphsEntry.buildParagraphsFromHistoryUri(i);
            ContentValues[] paragraphValues =
                    HistoryProvider.historyContentParser(i,
                            historyValues[i].getAsString(context.getString(R.string.history_raw_content)));

            cursor = context.getContentResolver().query(uri, null, null, null, null);

            emptyQueryError = "Erro: Nenhuma linha retornada da consulta de parágrafos para a história com id " + i + ".";
            assertTrue(emptyQueryError,
                    cursor.moveToFirst());

            int j = 0;
            do {
                String expectedResultDidntMatchActual =
                        "Valores de parágrafo atuais para a história com id " + i + " não batem com o esperado.";
                TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                        cursor,
                        paragraphValues[j++]);
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    /*
     * Este teste valida o método de delete do provider usando a URI de histórias
     * (CODE_HISTORIES, do matcher).
     *
     * Também checa se a tabela de parágrafos ficou vazia, visto que o DELETE CASCADE está
     * habilitado.
     */
    @Test
    public void testHistoryDelete() {
        testHistoriesBulkInsert();

        ContentValues[] historyValues =
                TestDbUtilities.createBulkInsertTestHistoryContentValues(context,
                        TestDbUtilities.CONTENT_VALUES_HIGHER_QUANTITY, true);

        Uri uri = HistoryContract.HistoriesEntry.CONTENT_URI;
        int deletedRows = context.getContentResolver().delete(uri, null, null);

        String deleteFailed = "Houveram falhas para deletar as histórias do database";
        assertEquals(deleteFailed, historyValues.length, deletedRows);

        HistoryDbHelper helper = new HistoryDbHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.query(HistoryContract.ParagraphsEntry.TABLE_NAME, null, null, null, null, null, null);

        String emptyQueryError = "Erro: Ainda existem registros na tabela de parágrafos";
        assertFalse(emptyQueryError,
                cursor.moveToFirst());

        cursor.close();
        database.close();
    }

    /*
     * Este teste valida o método de update do provider usando a URI para uma única história
     * (CODE_SINGLE_HISTORY, do matcher).
     *
     * Também valida o método de query do provider usando a URI de favoritos
     * (CODE_FAVORITES_HISTORIES, do matcher)
     *
     * Por fim, é feita uma verificação completa do banco.
     */
    @Test
    public void testSingleHistoryUpdate() {
        testHistoriesBulkInsert();
        ContentValues[] historyValues =
                TestDbUtilities.createBulkInsertTestHistoryContentValues(context,
                        TestDbUtilities.CONTENT_VALUES_HIGHER_QUANTITY, false);

        final int HISTORY_INDEX_TO_UPDATE = 2;

        historyValues[HISTORY_INDEX_TO_UPDATE].put(HistoryContract.HistoriesEntry.COLUMN_FAVORITE,
                HistoryContract.IS_FAVORITE);

        Uri uri = HistoryContract.HistoriesEntry.buildSingleHistoryUri(HISTORY_INDEX_TO_UPDATE);
        int rowsUpdated = context.getContentResolver().update(uri,
                historyValues[HISTORY_INDEX_TO_UPDATE], null, null);

        String updateFailed = "Houveram falhas para atualizar a história no database.";
        assertEquals(updateFailed, 1, rowsUpdated);

        uri = HistoryContract.HistoriesEntry.buildFavoritesUri();
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        updateFailed = "Quantidade incorreta de histórias favoritas no database.";
        assertEquals(updateFailed, 1, cursor.getCount());

        cursor.close();

        uri = HistoryContract.HistoriesEntry.CONTENT_URI;
        cursor = context.getContentResolver().query(uri, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta de histórias.";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        int i = 0;
        do {
            String expectedResultDidntMatchActual =
                    "Valores de histórias atuais não batem com o esperado.";
            TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                    cursor,
                    historyValues[i++]);
        } while (cursor.moveToNext());

        cursor.close();
    }

    /*
     * Este teste valida o método de update do provider usando a URI para várias história
     * (CODE_FAVORITES_HISTORIES, do matcher).
     *
     * Também valida o método de query do provider usando a URI de favoritos
     * (CODE_FAVORITES_HISTORIES, do matcher)
     *
     * Por fim, é feita uma verificação completa do banco.
     */
    @Test
    public void testHistoriesUpdate() {
        testHistoriesBulkInsert();
        ContentValues[] historyValues =
                TestDbUtilities.createBulkInsertTestHistoryContentValues(context,
                        TestDbUtilities.CONTENT_VALUES_HIGHER_QUANTITY, false);

        final String[] HISTORIES_INDEXES_TO_UPDATE = new String[]{"1", "2"};

        for (int i = 0; i < HISTORIES_INDEXES_TO_UPDATE.length; i++) {
            historyValues[Integer.parseInt(HISTORIES_INDEXES_TO_UPDATE[i])]
                    .put(HistoryContract.HistoriesEntry.COLUMN_FAVORITE, HistoryContract.IS_FAVORITE);
        }

        Uri uri = HistoryContract.HistoriesEntry.buildFavoritesUri();
        int rowsUpdated = context.getContentResolver().update(uri, null, null,
                HISTORIES_INDEXES_TO_UPDATE);

        String updateFailed = "Houveram falhas para atualizar as histórias no database.";
        assertEquals(updateFailed, HISTORIES_INDEXES_TO_UPDATE.length, rowsUpdated);

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        updateFailed = "Quantidade incorreta de histórias favoritas no database.";
        assertEquals(updateFailed, HISTORIES_INDEXES_TO_UPDATE.length, cursor.getCount());

        cursor.close();

        uri = HistoryContract.HistoriesEntry.CONTENT_URI;
        cursor = context.getContentResolver().query(uri, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta de histórias.";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        int i = 0;
        do {
            String expectedResultDidntMatchActual =
                    "Valores de histórias atuais não batem com o esperado.";
            TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                    cursor,
                    historyValues[i++]);
        } while (cursor.moveToNext());

        cursor.close();
    }

    /*
     * Este teste valida a estratégia de sincronização do banco com dados com a API, que consiste
     * nestes passos:
     * 1) Guardar os ID's das histórias marcadas como favoritas
     * 2) Deletar todas as histórias da base
     * 3) Recriar toda a base incluindo novas histórias
     * 4) Restaurar as marcações prévias de favoritos
     * 5) Indicar quantidade de novas histórias
     */
    @Test
    public void testSyncStrategy() {
        // 0) Preparar banco com apenas 3 histórias, sendo 2 favoritas

        Uri uri = HistoryContract.HistoriesEntry.CONTENT_URI;
        ContentValues[] oldHistoryValues =
                TestDbUtilities.createBulkInsertTestHistoryContentValues(context,
                        TestDbUtilities.CONTENT_VALUES_LOWER_QUANTITY, true);

        final String[] FAVORITE_INDEXES = new String[]{"1","2"};
//      Usar linha abaixo, em vez da de cima, para testar sincronia sem favoritos
//        final String[] FAVORITE_INDEXES = null;

        if(FAVORITE_INDEXES != null) {
            for (String index : FAVORITE_INDEXES) {
                oldHistoryValues[Integer.parseInt(index)]
                        .put(HistoryContract.HistoriesEntry.COLUMN_FAVORITE, HistoryContract.IS_FAVORITE);
            }
        }

        int rowsInserted = context.getContentResolver().bulkInsert(uri, oldHistoryValues);

        String bulkinsertFailed = "Houveram falhas para inserir as histórias antigas no database.";
        assertEquals(bulkinsertFailed, oldHistoryValues.length, rowsInserted);

        // 1) Guardar os ID's das histórias marcadas como favoritas

        uri = HistoryContract.HistoriesEntry.buildFavoritesUri();

        String[] projection = new String[]{HistoryContract.HistoriesEntry._ID};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        String[] favoritesSaved = null;
        if(cursor != null && cursor.moveToFirst()) {
            int i = 0;
            favoritesSaved = new String[cursor.getCount()];
            do {
                favoritesSaved[i++] = cursor.getString(0);
            } while (cursor.moveToNext());

            String expectedResultDidntMatchActual =
                    "ID's de histórias favoritas antigas não batem com o esperado.";
            assertEquals(expectedResultDidntMatchActual,
                    TextUtils.join(",", FAVORITE_INDEXES),
                    TextUtils.join(",", favoritesSaved));
        }
        cursor.close();

        // 2) Deletar todas as histórias da base

        uri = HistoryContract.HistoriesEntry.CONTENT_URI;
        int oldHistoryQuantity = context.getContentResolver().delete(uri, null, null);

        String deleteFailed = "Houveram falhas para deletar as histórias do database";
        assertEquals(deleteFailed, oldHistoryValues.length, oldHistoryQuantity);

        // 3) Recriar toda a base incluindo novas histórias

        ContentValues[] newHistoryValues =
                TestDbUtilities.createBulkInsertTestHistoryContentValues(context,
                        TestDbUtilities.CONTENT_VALUES_HIGHER_QUANTITY, true);

        int newHistoryQuantity = context.getContentResolver().bulkInsert(uri, newHistoryValues);

        bulkinsertFailed = "Houveram falhas para inserir as novas histórias no database.";
        assertEquals(bulkinsertFailed, newHistoryValues.length, newHistoryQuantity);

        // 4) Restaurar as marcações prévias de favoritos

        if(favoritesSaved != null) {
            uri = HistoryContract.HistoriesEntry.buildFavoritesUri();
            int rowsUpdated = context.getContentResolver().update(uri, null, null, favoritesSaved);

            String updateFailed = "Houveram falhas para atualizar as novas histórias no database.";
            assertEquals(updateFailed, favoritesSaved.length, rowsUpdated);

            cursor = context.getContentResolver().query(uri, projection, null, null, null);

            updateFailed = "Quantidade incorreta de histórias favoritas no database.";
            assertEquals(updateFailed, favoritesSaved.length, cursor.getCount());

            String emptyQueryError = "Erro: Nenhuma linha retornada da consulta de histórias favoritas novas.";
            assertTrue(emptyQueryError,
                    cursor.moveToFirst());

            int i = 0;
            String[] currentFavorites = new String[favoritesSaved.length];
            do {
                currentFavorites[i++] = cursor.getString(0);
            } while (cursor.moveToNext());
            cursor.close();

            String expectedResultDidntMatchActual =
                    "ID's de histórias favoritas novas não batem com o esperado.";
            assertEquals(expectedResultDidntMatchActual,
                    TextUtils.join(",", favoritesSaved),
                    TextUtils.join(",", currentFavorites));
        }

        // 5) Indicar quantidade de novas histórias

        String newHistoriesError = "A quantidade de novas histórias não bate com o esperado.";
        assertEquals(newHistoriesError,
                newHistoryValues.length - oldHistoryValues.length,
                newHistoryQuantity - oldHistoryQuantity);
    }

    private void deleteAllRecordsFromHistoriesTable() {
        HistoryDbHelper helper = new HistoryDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getWritableDatabase();

        database.delete(HistoryContract.HistoriesEntry.TABLE_NAME, null, null);

        database.close();
    }
}
