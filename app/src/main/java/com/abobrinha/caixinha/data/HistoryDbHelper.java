package com.abobrinha.caixinha.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;


class HistoryDbHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "histories.db";
    private static final int DATABASE_VERSION = 1;

    HistoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Método necessário para habilitar as constraints de FK no SQLite
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                db.setForeignKeyConstraintsEnabled(true);
            } else {
                db.execSQL("PRAGMA foreign_keys=ON;");
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_HISTORIES_TABLE =
                "CREATE TABLE " + HistoryContract.HistoriesEntry.TABLE_NAME + " (" +
                        HistoryContract.HistoriesEntry._ID + " INTEGER PRIMARY KEY, " +
                        HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE + " TEXT NOT NULL, " +
                        HistoryContract.HistoriesEntry.COLUMN_HISTORY_URL + " TEXT NOT NULL, " +
                        HistoryContract.HistoriesEntry.COLUMN_HISTORY_IMAGE + " TEXT, " +
                        HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE + " INTEGER NOT NULL, " +
                        HistoryContract.HistoriesEntry.COLUMN_HISTORY_MODIFIED + " INTEGER, " +
                        HistoryContract.HistoriesEntry.COLUMN_FAVORITE + " INTEGER NOT NULL DEFAULT " +
                        HistoryContract.IS_NOT_FAVORITE + ");";

        db.execSQL(SQL_CREATE_HISTORIES_TABLE);

        final String SQL_CREATE_PARAGRAPHS_TABLE =
                "CREATE TABLE " + HistoryContract.ParagraphsEntry.TABLE_NAME + " (" +
                        HistoryContract.ParagraphsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        HistoryContract.ParagraphsEntry.COLUMN_HISTORY_ID + " INTEGER REFERENCES " +
                        HistoryContract.HistoriesEntry.TABLE_NAME + " ON DELETE CASCADE, " +
                        HistoryContract.ParagraphsEntry.COLUMN_PARAGRAPH_TYPE + " INTEGER NOT NULL, " +
                        HistoryContract.ParagraphsEntry.COLUMN_PARAGRAPH_CONTENT + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_PARAGRAPHS_TABLE);

        final String SQL_CREATE_PARAGRAPHS_INDEX =
                "CREATE INDEX " + HistoryContract.ParagraphsEntry.TABLE_NAME + "_index ON " +
                        HistoryContract.ParagraphsEntry.TABLE_NAME + "(" +
                        HistoryContract.ParagraphsEntry.COLUMN_HISTORY_ID + ");";

        db.execSQL(SQL_CREATE_PARAGRAPHS_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + HistoryContract.HistoriesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HistoryContract.ParagraphsEntry.TABLE_NAME);
        onCreate(db);
    }
}
