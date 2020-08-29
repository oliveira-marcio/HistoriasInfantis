package com.abobrinha.caixinha.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.abobrinha.caixinha.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;


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

                String rawContent = values.getAsString(getContext().getString(R.string.history_raw_content));
                if (rawContent == null) {
                    error = "É necessário fornecer o conteúdo HTML da história.";
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
                if (favorite != null && (favorite < HistoryContract.IS_NOT_FAVORITE ||
                        favorite > HistoryContract.IS_FAVORITE)) {
                    error = "Status de favorito da história inválido.";
                }

                break;

            case HistoryContract.ParagraphsEntry.TABLE_NAME:
                Integer history_id = values.getAsInteger(HistoryContract.ParagraphsEntry.COLUMN_HISTORY_ID);
                if (history_id == null) {
                    error = "É necessário fornecer o ID da história (conforme Wordpress).";
                }

                Integer type = values.getAsInteger(HistoryContract.ParagraphsEntry.COLUMN_PARAGRAPH_TYPE);
                if (type == null || !HistoryContract.ParagraphsEntry.isValidType(type)) {
                    error = "É necessário fornecer um tipo válido de parágrafo";
                }

                String content = values.getAsString(HistoryContract.ParagraphsEntry.COLUMN_PARAGRAPH_CONTENT);
                if (content == null) {
                    error = "É necessário fornecer o conteúdo do parágrafo";
                }

                break;
        }

        return error;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new HistoryDbHelper(getContext());
        return true;
    }

    /**
     * Método faz parsing do conteúdo HTML da história retornardo pela API do Wordpress e tenta
     * gerar um conteúdo sem tags, quebrado em parágrados e identificados por tipos específicos
     * como texto da história, autor, imagem e fim da história para serem guardados no banco dessa
     * forma e posteriormentente utilizados na RecyclerView da história com os layouts apropriados
     * por tipo.
     *
     * Por conta de alterações recentes no editor online do Wordpress, as imagens são geradas em
     * tags FIGURE fora de parágrafos (P), então para que as imagens sejam corretamente parseadas,
     * as tags FIGURE serão renomeadas para P antes do parsing.
     */
    public static ContentValues[] historyContentParser(long id, String htmlContent) {

        final String BR_TOKEN = "#!#br2n#!#";
        final String TAG_P = "p";
        final String TAG_IMG = "img";
        final String TAG_SRC = "src";
        final String TAG_FIGURE = "figure";

        List<ContentValues> historyValues = new ArrayList<>();

        Document doc = Jsoup.parse(
                htmlContent
                        .replaceAll("(?i)<br[^>]*>", BR_TOKEN)
                        .replaceAll("<((\\\\/)?)" + TAG_FIGURE, "<$1" + TAG_P)
        );

        for (Element p : doc.select(TAG_P)) {
            if (!p.text().trim().isEmpty()) {
                String paragraphString = p.text().replaceAll(BR_TOKEN, "\n");
                int paragraphType;
                boolean isAuthor = false;

                if (paragraphString.trim().toLowerCase()
                        .equals(HistoryContract.ParagraphsEntry.AUTHOR.toLowerCase())) {
                    paragraphType = HistoryContract.ParagraphsEntry.TYPE_AUTHOR;
                    isAuthor = true;
                } else if (paragraphString.trim().toLowerCase()
                        .equals(HistoryContract.ParagraphsEntry.END.toLowerCase())) {
                    paragraphType = HistoryContract.ParagraphsEntry.TYPE_END;
                } else {
                    paragraphType = HistoryContract.ParagraphsEntry.TYPE_TEXT;
                }

                ContentValues paragraphText = new ContentValues();
                paragraphText.put(HistoryContract.ParagraphsEntry.COLUMN_HISTORY_ID, id);
                paragraphText.put(HistoryContract.ParagraphsEntry.COLUMN_PARAGRAPH_TYPE, paragraphType);
                paragraphText.put(HistoryContract.ParagraphsEntry.COLUMN_PARAGRAPH_CONTENT, paragraphString);
                historyValues.add(paragraphText);

                if (isAuthor) break;
            }

            ContentValues paragraphImage = new ContentValues();
            for (Element img : p.select(TAG_IMG)) {
                String imageUrl = img.absUrl(TAG_SRC).contains("?") ?
                        img.absUrl(TAG_SRC).substring(0, img.absUrl(TAG_SRC).indexOf("?")) :
                        img.absUrl(TAG_SRC);

                paragraphImage.put(HistoryContract.ParagraphsEntry.COLUMN_HISTORY_ID, id);
                paragraphImage.put(HistoryContract.ParagraphsEntry.COLUMN_PARAGRAPH_TYPE,
                        HistoryContract.ParagraphsEntry.TYPE_IMAGE);
                paragraphImage.put(HistoryContract.ParagraphsEntry.COLUMN_PARAGRAPH_CONTENT, imageUrl);
                historyValues.add(paragraphImage);
            }
        }
        return historyValues.toArray(new ContentValues[0]);
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        switch (sUriMatcher.match(uri)) {
            case CODE_HISTORIES:
                final String historyTableName = HistoryContract.HistoriesEntry.TABLE_NAME;
                final String paragraphTableName = HistoryContract.ParagraphsEntry.TABLE_NAME;
                final String idHistoryColumnName = HistoryContract.HistoriesEntry._ID;
                final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                final String historyRawContentColumnName =
                        getContext().getString(R.string.history_raw_content);

                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        String errorConstraints = checkTableConstraints(historyTableName, value);
                        if (errorConstraints != null) {
                            throw new IllegalArgumentException(errorConstraints);
                        }

                        if (value.size() == 0) {
                            continue;
                        }

                        ContentValues[] paragraphValues =
                                historyContentParser(value.getAsLong(idHistoryColumnName),
                                        value.getAsString(historyRawContentColumnName));

                        value.remove(historyRawContentColumnName);

                        long _id = db.insert(historyTableName, null, value);

                        if (_id != -1) {
                            for (ContentValues paragraphValue : paragraphValues) {
                                String paragraphErrorConstraints =
                                        checkTableConstraints(paragraphTableName, paragraphValue);
                                if (paragraphErrorConstraints != null) {
                                    throw new IllegalArgumentException(paragraphErrorConstraints);
                                }

                                if (value.size() == 0) {
                                    continue;
                                }

                                db.insert(paragraphTableName, null, paragraphValue);
                            }
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                throw new UnsupportedOperationException("URI desconhecida: " + uri);
        }
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
        int numRowsDeleted;
        String tableName;

        switch (sUriMatcher.match(uri)) {

            case CODE_HISTORIES:
                tableName = HistoryContract.HistoriesEntry.TABLE_NAME;

                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        tableName,
                        selection,
                        selectionArgs);

                if (numRowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return numRowsDeleted;

            default:
                throw new UnsupportedOperationException("URI desconhecida ou inválida para deleção: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        ContentValues selectedValues = new ContentValues();
        String favoriteColumnName = HistoryContract.HistoriesEntry.COLUMN_FAVORITE;

        switch (sUriMatcher.match(uri)) {

            // Remove o status de favorito de todas as histórias
            case CODE_HISTORIES:
                selection = favoriteColumnName + "=?";
                selectionArgs = new String[]{String.valueOf(HistoryContract.IS_FAVORITE)};
                selectedValues.put(favoriteColumnName, HistoryContract.IS_NOT_FAVORITE);
                break;

            // Update de uma história específica deverá ser apenas do status de favorito
            case CODE_SINGLE_HISTORY:
                selection = HistoryContract.HistoriesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                if (values.containsKey(favoriteColumnName)) {
                    Integer favorite = values.getAsInteger(favoriteColumnName);
                    if (favorite != null && (favorite < HistoryContract.IS_NOT_FAVORITE
                            || favorite > HistoryContract.IS_FAVORITE)) {
                        throw new IllegalArgumentException("Status de favorito inválido.");
                    }

                    selectedValues.put(favoriteColumnName, favorite);
                }

                break;

            // Marca todas as histórias selecionadas como favoritas
            case CODE_FAVORITES_HISTORIES:
                selection = HistoryContract.HistoriesEntry._ID + " IN (" +
                        TextUtils.join(",", selectionArgs) + ")";
                selectionArgs = null;
                selectedValues.put(favoriteColumnName, HistoryContract.IS_FAVORITE);
                break;

            default:
                throw new UnsupportedOperationException("URI desconhecida: " + uri);
        }

        int rowsUpdated = mOpenHelper.getWritableDatabase().update(
                HistoryContract.HistoriesEntry.TABLE_NAME,
                selectedValues,
                selection,
                selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            Uri uriAllFavorites = HistoryContract.HistoriesEntry.buildFavoritesUri();
            getContext().getContentResolver().notifyChange(uriAllFavorites, null);
        }

        return rowsUpdated;
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
