package com.abobrinha.caixinha.network;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WordPressJson {

    private final static String WORDPRESS_POST_ID = "ID";
    private final static String WORDPRESS_POST_DATE = "date";
    private final static String WORDPRESS_POST_MODIFIED = "modified";
    private final static String WORDPRESS_POST_TITLE = "title";
    private final static String WORDPRESS_POST_URL = "URL";
    private final static String WORDPRESS_POST_IMAGE = "featured_image";
    private final static String WORDPRESS_POST_CONTENT = "content";

    private WordPressJson() {
    }

    /*
     * Retorna os campos JSON referente a um post do WordPress. Útil para filtrar a String JSON
     * retornada pelo servidor na classe WordPressJson por apenas estes campos.
     */
    public static String getJsonHistoryFields() {
        return WORDPRESS_POST_ID + "," +
                WORDPRESS_POST_DATE + "," +
                WORDPRESS_POST_MODIFIED + "," +
                WORDPRESS_POST_TITLE + "," +
                WORDPRESS_POST_URL + "," +
                WORDPRESS_POST_IMAGE + "," +
                WORDPRESS_POST_CONTENT;
    }

    public static List<ContentValues> getHistoriesFromJson(Context context, String historiesJsonStr)
            throws JSONException {

        final String WORDPRESS_RESULTS = "posts";
        final String WORDPRESS_ERROR_TYPE = "error";
        final String WORDPRESS_ERROR_MESSAGE = "message";

        if (TextUtils.isEmpty(historiesJsonStr)) {
            return null;
        }

        JSONObject baseJsonResponse = new JSONObject(historiesJsonStr);
        if (baseJsonResponse.has(WORDPRESS_ERROR_TYPE)) {
            String errorType = baseJsonResponse.getString(WORDPRESS_ERROR_TYPE);
            String errorMessage = baseJsonResponse.getString(WORDPRESS_ERROR_MESSAGE);
            throw new JSONException(String.format("API Error (%s): %s", errorType, errorMessage));
        }

        JSONArray historyArray = baseJsonResponse.getJSONArray(WORDPRESS_RESULTS);

        if (historyArray == null || historyArray.length() == 0) return null;

        List<ContentValues> historiesValues = new ArrayList<>();

        for (int i = 0; i < historyArray.length(); i++) {
            JSONObject currentHistory = historyArray.getJSONObject(i);

            long id = currentHistory.getLong(WORDPRESS_POST_ID);
            String title = currentHistory.getString(WORDPRESS_POST_TITLE);
            String urlHistory = currentHistory.getString(WORDPRESS_POST_URL);
            String urlImage = currentHistory.getString(WORDPRESS_POST_IMAGE);
            String content = currentHistory.getString(WORDPRESS_POST_CONTENT);
            long dateCreatedInMillis = dateInMillis(currentHistory.getString(WORDPRESS_POST_DATE));
            long dateModifiedInMillis =
                    dateInMillis(currentHistory.optString(WORDPRESS_POST_MODIFIED));

            ContentValues historyValues = new ContentValues();
            historyValues.put(HistoryContract.HistoriesEntry._ID, id);
            historyValues.put(HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE, title);
            historyValues.put(HistoryContract.HistoriesEntry.COLUMN_HISTORY_URL, urlHistory);
            historyValues.put(HistoryContract.HistoriesEntry.COLUMN_HISTORY_IMAGE, urlImage);
            historyValues.put(HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE, dateCreatedInMillis);
            historyValues.put(HistoryContract.HistoriesEntry.COLUMN_HISTORY_MODIFIED, dateModifiedInMillis);
            historyValues.put(context.getString(R.string.history_raw_content), content);

            historiesValues.add(historyValues);
        }

        return historiesValues;
    }

    /**
     * Converte uma data em String para milisegundos
     * Ex de data retornada pela API: "2017-05-06T08:00:19-03:00"
     */
    public static long dateInMillis(String dateString) {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
        try {
            Date date = formatter.parse(dateString.replaceFirst(":(?=[0-9]{2}$)", ""));
            return date.getTime();
        } catch (java.text.ParseException e) {
            return -1;
        }
    }
}