package com.abobrinha.caixinha.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract.HistoriesEntry;
import com.abobrinha.caixinha.data.HistoryContract.ParagraphsEntry;

import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

public class TestDbUtilities {
    static int VALID_HISTORY_ID = 123;
    static int INVALID_HISTORY_ID = 1234;

    static final int CONTENT_VALUES_HIGHER_QUANTITY = 5;
    static final int CONTENT_VALUES_LOWER_QUANTITY = 3;

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int index = valueCursor.getColumnIndex(columnName);

            String columnNotFoundError = "Coluna '" + columnName + "' não encontrada. " + error;
            assertFalse(columnNotFoundError, index == -1);

            String expectedValue = entry.getValue().toString();
            String actualValue = valueCursor.getString(index);

            String valuesDontMatchError = "[Coluna '" + columnName + "'] Valor atual '" + actualValue
                    + "' não bate com o valor esperado '" + expectedValue + "'. "
                    + error;

            assertEquals(valuesDontMatchError,
                    expectedValue,
                    actualValue);
        }
    }

    static ContentValues createTestHistoryContentValues() {
        ContentValues testHistoryValues = new ContentValues();

        testHistoryValues.put(HistoriesEntry._ID, VALID_HISTORY_ID);
        testHistoryValues.put(HistoriesEntry.COLUMN_HISTORY_TITLE, "História");
        testHistoryValues.put(HistoriesEntry.COLUMN_HISTORY_URL, "http://www.teste.com/" + VALID_HISTORY_ID);
        testHistoryValues.put(HistoriesEntry.COLUMN_HISTORY_IMAGE, "http://www.teste.com/" + VALID_HISTORY_ID + "/image.jpg");
        testHistoryValues.put(HistoriesEntry.COLUMN_HISTORY_DATE, 12345);
        testHistoryValues.put(HistoriesEntry.COLUMN_HISTORY_MODIFIED, 54321);
        testHistoryValues.put(HistoriesEntry.COLUMN_FAVORITE, 0);

        return testHistoryValues;
    }

    static ContentValues[] createBulkInsertTestHistoryContentValues(Context context, int quantity, boolean addRawContent) {
        ContentValues[] testHistoryValues = new ContentValues[quantity];

        for (int i = 0; i < testHistoryValues.length; i++) {
            testHistoryValues[i] = new ContentValues();
            testHistoryValues[i].put(HistoriesEntry._ID, i);
            testHistoryValues[i].put(HistoriesEntry.COLUMN_HISTORY_TITLE, "História" + i);
            testHistoryValues[i].put(HistoriesEntry.COLUMN_HISTORY_URL, "http://www.teste.com/" + i);
            testHistoryValues[i].put(HistoriesEntry.COLUMN_HISTORY_IMAGE, "http://www.teste.com/" + i + "/image.jpg");
            testHistoryValues[i].put(HistoriesEntry.COLUMN_HISTORY_DATE, i + 1000);
            testHistoryValues[i].put(HistoriesEntry.COLUMN_HISTORY_MODIFIED, i + 1500);
            if (addRawContent) {
                String rawContent = "<p>raw qualquer " + i + "</p>" +
                        "<p>Mais uma linha</p>" +
                        "<p><img src='http://www.teste.com/" + i + "/image.jpg'></p>" +
                        "<p>" + ParagraphsEntry.END + "</p>" +
                        "<p>" + ParagraphsEntry.AUTHOR + "</p>";
                testHistoryValues[i].put(context.getString(R.string.history_raw_content), rawContent);
            }
        }

        return testHistoryValues;
    }

    static ContentValues[] createBulkInsertTestParagraphsContentValues(long id, int quantity) {
        ContentValues[] testParagraphsValues = new ContentValues[quantity];

        for (int i = 0; i < testParagraphsValues.length; i++) {
            testParagraphsValues[i] = new ContentValues();
            testParagraphsValues[i].put(ParagraphsEntry.COLUMN_HISTORY_ID, id);
            testParagraphsValues[i].put(ParagraphsEntry.COLUMN_PARAGRAPH_TYPE, ParagraphsEntry.TYPE_TEXT);
            testParagraphsValues[i].put(ParagraphsEntry.COLUMN_PARAGRAPH_CONTENT, "Paragraph " + i);
        }

        return testParagraphsValues;
    }

    static ContentValues createTestParagraphContentValues(boolean validHistoryId) {
        ContentValues testParagraphValues = new ContentValues();

        testParagraphValues.put(ParagraphsEntry.COLUMN_HISTORY_ID,
                (validHistoryId ? VALID_HISTORY_ID : INVALID_HISTORY_ID));
        testParagraphValues.put(ParagraphsEntry.COLUMN_PARAGRAPH_TYPE, ParagraphsEntry.TYPE_TEXT);
        testParagraphValues.put(ParagraphsEntry.COLUMN_PARAGRAPH_CONTENT, "Parágrafo qualquer.");

        return testParagraphValues;
    }
}