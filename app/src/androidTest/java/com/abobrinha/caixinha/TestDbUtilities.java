package com.abobrinha.caixinha;


import android.content.ContentValues;
import android.database.Cursor;

import com.abobrinha.caixinha.data.HistoryContract.HistoriesEntry;
import com.abobrinha.caixinha.data.HistoryContract.ParagraphsEntry;

import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

public class TestDbUtilities {
    static int VALID_HISTORY_ID = 123;
    static int INVALID_HISTORY_ID = 1234;

    static final int CONTENT_VALUES_QUANTITY = 5;

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int index = valueCursor.getColumnIndex(columnName);

            /* Test to see if the column is contained within the cursor */
            String columnNotFoundError = "Coluna '" + columnName + "' não encontrada. " + error;
            assertFalse(columnNotFoundError, index == -1);

            /* Test to see if the expected value equals the actual value (from the Cursor) */
            String expectedValue = entry.getValue().toString();
            String actualValue = valueCursor.getString(index);

            String valuesDontMatchError = "Valor atual '" + actualValue
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

    static ContentValues[] createBulkInsertTestHistoryContentValues() {
        ContentValues[] testHistoryValues = new ContentValues[CONTENT_VALUES_QUANTITY];

        for (int i = 0; i < testHistoryValues.length; i++) {
            testHistoryValues[i] = new ContentValues();
            testHistoryValues[i].put(HistoriesEntry._ID, i);
            testHistoryValues[i].put(HistoriesEntry.COLUMN_HISTORY_TITLE, "História" + i);
            testHistoryValues[i].put(HistoriesEntry.COLUMN_HISTORY_URL, "http://www.teste.com/" + i);
            testHistoryValues[i].put(HistoriesEntry.COLUMN_HISTORY_IMAGE, "http://www.teste.com/" + i + "/image.jpg");
            testHistoryValues[i].put(HistoriesEntry.COLUMN_HISTORY_DATE, i + 1000);
            testHistoryValues[i].put(HistoriesEntry.COLUMN_HISTORY_MODIFIED, i + 1500);
        }

        return testHistoryValues;
    }

    static ContentValues[] createBulkInsertTestParagraphsContentValues() {
        ContentValues[] testParagraphsValues = new ContentValues[CONTENT_VALUES_QUANTITY];

        for (int i = 0; i < testParagraphsValues.length; i++) {
            testParagraphsValues[i] = new ContentValues();
            testParagraphsValues[i].put(ParagraphsEntry.COLUMN_HISTORY_ID, VALID_HISTORY_ID);
            testParagraphsValues[i].put(ParagraphsEntry.COLUMN_PARAGRAPH_TYPE, "Type " + i);
            testParagraphsValues[i].put(ParagraphsEntry.COLUMN_PARAGRAPH_CONTENT, "Paragraph " + i);
        }

        return testParagraphsValues;
    }

    static ContentValues createTestParagraphContentValues(boolean validHistoryId) {
        ContentValues testParagraphValues = new ContentValues();

        testParagraphValues.put(ParagraphsEntry.COLUMN_HISTORY_ID,
                (validHistoryId ? VALID_HISTORY_ID : INVALID_HISTORY_ID));
        testParagraphValues.put(ParagraphsEntry.COLUMN_PARAGRAPH_TYPE, "Text");
        testParagraphValues.put(ParagraphsEntry.COLUMN_PARAGRAPH_CONTENT, "Parágrafo qualquer.");

        return testParagraphValues;
    }
}