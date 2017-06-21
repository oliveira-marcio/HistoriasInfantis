package com.abobrinha.caixinha.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

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

    @Test
    public void testSingleHistoryUpdate() {

    }

    @Test
    public void testHistoriesUpdate() {

    }

    @Test
    public void testSyncStrategy() {

    }

    private void deleteAllRecordsFromHistoriesTable() {
        HistoryDbHelper helper = new HistoryDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getWritableDatabase();

        database.delete(HistoryContract.HistoriesEntry.TABLE_NAME, null, null);

        database.close();
    }
}
