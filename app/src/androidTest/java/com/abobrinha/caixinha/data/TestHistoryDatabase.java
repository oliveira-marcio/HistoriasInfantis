package com.abobrinha.caixinha.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class TestHistoryDatabase {
    private final Context context = InstrumentationRegistry.getTargetContext();
    private SQLiteDatabase database;
    private HistoryDbHelper dbHelper;

    private final String HISTORIES_TABLE = HistoryContract.HistoriesEntry.TABLE_NAME;
    private final String PARAGRAPHS_TABLE = HistoryContract.ParagraphsEntry.TABLE_NAME;

    @Before
    public void before() {
        try {
            dbHelper = new HistoryDbHelper(context);
            context.deleteDatabase(HistoryDbHelper.DATABASE_NAME);
            database = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {
        if (database != null)
            database.close();
    }

    /**
     * Testa a criação do DB e checa se todas as tabelas esperadas estão presentes bem como as
     * suas respectivas colunas.
     */
    @Test
    public void testCreateDb() {
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(HISTORIES_TABLE);
        tableNameHashSet.add(PARAGRAPHS_TABLE);

        String error = "Database não pôde ser aberto";
        assertEquals(error,
                true,
                database.isOpen());

        Cursor tableNameCursor = database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table'",
                null);

        String errorInCreatingDatabase =
                "Erro: Database não foi criado corretamente.";
        assertTrue(errorInCreatingDatabase,
                tableNameCursor.moveToFirst());
        do {
            tableNameHashSet.remove(tableNameCursor.getString(0));
        } while (tableNameCursor.moveToNext());

        assertTrue("Erro: Database criado sem as tabelas esperadas.",
                tableNameHashSet.isEmpty());

        tableNameCursor.close();

        final ArrayList<String> tableNames = new ArrayList<>();
        tableNames.add(HISTORIES_TABLE);
        tableNames.add(PARAGRAPHS_TABLE);

        final List<HashSet<String>> tableColumnsHashSetArray = new ArrayList<HashSet<String>>();

        for (int i = 0; i < tableNames.size(); i++) {
            tableColumnsHashSetArray.add(new HashSet<String>());
        }

        tableColumnsHashSetArray.get(0).add(HistoryContract.HistoriesEntry._ID);
        tableColumnsHashSetArray.get(0).add(HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE);
        tableColumnsHashSetArray.get(0).add(HistoryContract.HistoriesEntry.COLUMN_HISTORY_URL);
        tableColumnsHashSetArray.get(0).add(HistoryContract.HistoriesEntry.COLUMN_HISTORY_IMAGE);
        tableColumnsHashSetArray.get(0).add(HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE);
        tableColumnsHashSetArray.get(0).add(HistoryContract.HistoriesEntry.COLUMN_HISTORY_MODIFIED);
        tableColumnsHashSetArray.get(0).add(HistoryContract.HistoriesEntry.COLUMN_FAVORITE);

        tableColumnsHashSetArray.get(1).add(HistoryContract.ParagraphsEntry._ID);
        tableColumnsHashSetArray.get(1).add(HistoryContract.ParagraphsEntry.COLUMN_HISTORY_ID);
        tableColumnsHashSetArray.get(1).add(HistoryContract.ParagraphsEntry.COLUMN_PARAGRAPH_TYPE);
        tableColumnsHashSetArray.get(1).add(HistoryContract.ParagraphsEntry.COLUMN_PARAGRAPH_CONTENT);

        for (int i = 0; i < tableNames.size(); i++) {
            tableNameCursor = database.query(tableNames.get(i), null, null, null, null, null, null);
            String[] tableColumnNames = tableNameCursor.getColumnNames();
            tableNameCursor.close();

            for (String tableColumnName : tableColumnNames) {
                tableColumnsHashSetArray.get(i).remove(tableColumnName);
            }

            assertTrue("Erro: Tabela '" + tableNames.get(i) + "' criada sem as colunas esperadas.",
                    tableColumnsHashSetArray.get(i).isEmpty());
        }
    }

    /**
     * Testa a inclusão de um único registro na tabela de histórias e checa se os dados foram
     * armazenados conforme esperado
     */
    @Test
    public void testInsertSingleRecordIntoHistoryTable() {
        ContentValues testValues = TestDbUtilities.createTestHistoryContentValues();

        long rowId = database.insert(HISTORIES_TABLE, null, testValues);

        int valueOfIdIfInsertFails = -1;
        String insertFailed = "Não foi possível inserir no database";
        assertTrue(insertFailed,
                valueOfIdIfInsertFails != rowId);

        Cursor cursor = database.query(HISTORIES_TABLE, null, null, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta na tabela de histórias";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        String expectedResultDidntMatchActual =
                "Valores de histórias atuais não batem com o esperado.";
        TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                cursor,
                testValues);

        assertFalse("Erro: Mais de uma linha retornada da consulta de histórias",
                cursor.moveToNext());

        cursor.close();
    }

    /**
     * Testa a inclusão de vários registro na tabela de histórias e checa se os dados foram
     * armazenados conforme esperado.
     */
    @Test
    public void testBulkInsertHistories() {

        ContentValues[] historiesValues =
                TestDbUtilities.createBulkInsertTestHistoryContentValues(context,
                        TestDbUtilities.CONTENT_VALUES_HIGHER_QUANTITY, false);

        int rowsInserted = 0;
        database.beginTransaction();
        for (ContentValues value : historiesValues) {
            long _id = database.insert(HISTORIES_TABLE, null, value);
            if (_id != -1) {
                rowsInserted++;
            }
        }
        database.setTransactionSuccessful();
        database.endTransaction();


        int expectedInserts = historiesValues.length;
        String insertFailed = "Não foi possível inserir no database";

        assertEquals(insertFailed,
                expectedInserts, rowsInserted);

        Cursor cursor = database.query(HISTORIES_TABLE, null, null, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta na tabela de histórias";

        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        int i = 0;
        do {
            String expectedResultDidntMatchActual =
                    "Valores de histórias atuais não batem com o esperado.";
            TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                    cursor,
                    historiesValues[i++]);
        } while (cursor.moveToNext());

        cursor.close();
    }

    /**
     * Testa a inclusão de um único registro na tabela de parágrafos e checa se os dados foram
     * armazenados conforme esperado.
     * <p>
     * OBS: Foi necessário incluir antes um registro na tabela-pai (histórias).
     */
    @Test
    public void testInsertSingleRecordIntoParagraphsTable() {
        testInsertSingleRecordIntoHistoryTable();

        // Força a criação de um registro com ID válido para a FK
        ContentValues testValues = TestDbUtilities.createTestParagraphContentValues(true);

        long rowId = database.insert(PARAGRAPHS_TABLE, null, testValues);

        int valueOfIdIfInsertFails = -1;
        String insertFailed = "Não foi possível inserir no database";
        assertTrue(insertFailed,
                valueOfIdIfInsertFails != rowId);

        Cursor cursor = database.query(PARAGRAPHS_TABLE, null, null, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta na tabela de parágrafos";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        String expectedResultDidntMatchActual =
                "Valores de parágrafos atuais não batem com o esperado.";
        TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                cursor,
                testValues);

        assertFalse("Erro: Mais de uma linha retornada da consulta de parágrafos",
                cursor.moveToNext());

        cursor.close();
    }

    /**
     * Testa a inclusão de vários registros na tabela de parágrafos e checa se os dados foram
     * armazenados conforme esperado.
     * <p>
     * OBS: Foi necessário incluir antes um registro na tabela-pai (histórias).
     */
    @Test
    public void testBulkInsertParagraphs() {
        testInsertSingleRecordIntoHistoryTable();

        ContentValues[] paragraphsValues =
                TestDbUtilities.createBulkInsertTestParagraphsContentValues(
                        TestDbUtilities.VALID_HISTORY_ID, TestDbUtilities.CONTENT_VALUES_HIGHER_QUANTITY);

        int rowsInserted = 0;
        database.beginTransaction();
        for (ContentValues value : paragraphsValues) {
            long _id = database.insert(PARAGRAPHS_TABLE, null, value);
            if (_id != -1) {
                rowsInserted++;
            }
        }
        database.setTransactionSuccessful();
        database.endTransaction();


        int expectedInserts = paragraphsValues.length;
        String insertFailed = "Não foi possível inserir no database";

        assertEquals(insertFailed,
                expectedInserts, rowsInserted);

        Cursor cursor = database.query(PARAGRAPHS_TABLE, null, null, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta na tabela de parágrafos";

        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        int i = 0;
        do {
            String expectedResultDidntMatchActual =
                    "Valores de parágrafos atuais não batem com o esperado.";
            TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                    cursor,
                    paragraphsValues[i++]);
        } while (cursor.moveToNext());

        cursor.close();
    }

    /**
     * Testa a constraint de FK da tabela de parágrafos.
     */
    @Test
    public void testParagraphsTableFkConstraint() {
        testInsertSingleRecordIntoHistoryTable();

        // Força a criação de um registro com ID inválido para a FK
        ContentValues testValues = TestDbUtilities.createTestParagraphContentValues(false);

        long rowId = database.insert(PARAGRAPHS_TABLE, null, testValues);

        int valueOfIdIfInsertFails = -1;
        String insertFailed = "Erro; Registro inserido indevidamente. A FK constraint falhou.";
        assertEquals(insertFailed,
                valueOfIdIfInsertFails,
                rowId);
    }

    /**
     * Testa a deleção em cascata da tabela de filmes para a tabela de parágrafos.
     */
    @Test
    public void testDeleteCascadeOnTrailersTable() {
        testInsertSingleRecordIntoHistoryTable();

        ContentValues testValues = TestDbUtilities.createTestParagraphContentValues(true);
        long rowId = database.insert(PARAGRAPHS_TABLE, null, testValues);

        int valueOfIdIfInsertFails = -1;
        String insertFailed = "Não foi possível inserir no database";
        assertTrue(insertFailed,
                valueOfIdIfInsertFails != rowId);

        int rowsDeleted = database.delete(HISTORIES_TABLE, null, null);
        int rowsExpectedToBeDeleted = 1;
        String deleteFailed = "Erro: Problemas na exclusão de dados da tabela de histórias.";
        assertEquals(deleteFailed,
                rowsExpectedToBeDeleted,
                rowsDeleted);

        Cursor cursor = database.query(PARAGRAPHS_TABLE, null, null, null, null, null, null);
        String deleteCascadeError = "Erro: A deleção em cascata falhou para a tabela de parágrafos.";
        assertFalse(deleteCascadeError, cursor.moveToFirst());
        cursor.close();
    }
}