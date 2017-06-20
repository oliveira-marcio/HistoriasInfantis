package com.abobrinha.caixinha.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import static android.R.attr.key;


public class HistoryProvider extends ContentProvider {
    public static final int CODE_HISTORIES = 100;
    public static final int CODE_FAVORITES_HISTORIES = 101;
    public static final int CODE_SINGLE_HISTORY = 200;
    public static final int CODE_PARAGRAPHS = 201;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private HistoryDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = HistoryContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, HistoryContract.PATH_HISTORIES, CODE_HISTORIES);

        matcher.addURI(authority, HistoryContract.PATH_HISTORIES + "/" +
                HistoryContract.PATH_FAVORITES, CODE_FAVORITES_HISTORIES);

        matcher.addURI(authority,
                HistoryContract.PATH_HISTORIES + "/#", CODE_SINGLE_HISTORY);

        matcher.addURI(authority,
                HistoryContract.PATH_HISTORIES + "/#/" + HistoryContract.PATH_PARAGRAPHS,
                CODE_PARAGRAPHS);

        return matcher;
    }

    private String checkTableConstraints(String tableName, ContentValues values) {
        String error = null;
        switch (tableName) {
            case HistoryContract.HistoriesEntry.TABLE_NAME:
                Integer id = values.getAsInteger(HistoryContract.HistoriesEntry._ID);
                if (id == null) {
                    error = "É necessário fornecer o ID da história (conforme Wordpress).";
                }

                String title = values.getAsString(HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE);
                if (title == null) {
                    error = "É necessário fornecer o título da história.";
                }

                String url = values.getAsString(HistoryContract.HistoriesEntry.COLUMN_HISTORY_URL);
                if (url == null) {
                    error = "É necessário fornecer a URL da história.";
                }

                Long date_created = values.getAsLong(HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE);
                if (date_created == null || date_created < 0) {
                    error = "É necessário fornecer a data de criação da história.";
                }

                Integer favorite = values.getAsInteger(HistoryContract.HistoriesEntry.COLUMN_FAVORITE);
                if (favorite < HistoryContract.IS_NOT_FAVORITE ||
                        favorite > HistoryContract.IS_FAVORITE) {
                    error = "Status de favorito da história inválido.";
                }

                break;

            case HistoryContract.ParagraphsEntry.TABLE_NAME:
                Integer history_id = values.getAsInteger(HistoryContract.ParagraphsEntry.COLUMN_HISTORY_ID);
                if (history_id == null) {
                    error = "É necessário fornecer o ID da história (conforme Wordpress).";
                }

                String type = values.getAsString(HistoryContract.ParagraphsEntry.COLUMN_PARAGRAPH_TYPE);
                if (type == null) {
                    error = "É necessário fornecer o tipo do parágrafo";
                }

                String content = values.getAsString(HistoryContract.ParagraphsEntry.COLUMN_PARAGRAPH_CONTENT);
                if (content == null) {
                    error = "É necessário fornecer o conteúdo do parágrafo";
                }

                break;        }

        return error;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new HistoryDbHelper(getContext());
        return true;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, ContentValues[] values) {

        switch (sUriMatcher.match(uri)) {
            case CODE_HISTORIES:
                // Bulkinsert de histórias não deve alterar os status de favoritos existentes
                for (ContentValues value : values) {
                    if (value.containsKey(HistoryContract.HistoriesEntry.COLUMN_FAVORITE)) {
                        value.remove(HistoryContract.HistoriesEntry.COLUMN_FAVORITE);
                    }
                }
                return bulkUpsertHistories(uri, values);

            default:
                throw new UnsupportedOperationException("URI desconhecida: " + uri);
        }
    }

    private int bulkUpsertHistories(Uri uri, ContentValues[] values) {
        return 0;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        String tableName;

        switch (sUriMatcher.match(uri)) {
            case CODE_HISTORIES:
                tableName = HistoryContract.HistoriesEntry.TABLE_NAME;
                break;

            case CODE_FAVORITES_HISTORIES:
                tableName = HistoryContract.HistoriesEntry.TABLE_NAME;
                selection = HistoryContract.HistoriesEntry.COLUMN_FAVORITE + "=" + HistoryContract.IS_FAVORITE;
                break;

            case CODE_SINGLE_HISTORY:
                tableName = HistoryContract.HistoriesEntry.TABLE_NAME;
                selection = HistoryContract.HistoriesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;

            case CODE_PARAGRAPHS:
                tableName = HistoryContract.ParagraphsEntry.TABLE_NAME;
                selection = HistoryContract.ParagraphsEntry.COLUMN_HISTORY_ID + "=?";
                selectionArgs = new String[]{uri.getPathSegments().get(1)};
                break;

            default:
                throw new UnsupportedOperationException("URI desconhecida: " + uri);

        }

        cursor = mOpenHelper.getReadableDatabase().query(
                tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new RuntimeException(
                "Não há implementação de insert neste aplicativo. Use bulkUpsertHistories ou " +
                        "bulkUpsertParagraphs no lugar.");
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("Não há implementação de getType neste aplicativo.");
    }
}
