package com.abobrinha.caixinha.network;

import android.text.TextUtils;

import com.abobrinha.caixinha.data.History;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WordPressJson {

    final static String WORDPRESS_POST_ID = "ID";
    final static String WORDPRESS_POST_DATE = "date";
    final static String WORDPRESS_POST_MODIFIED = "modified";
    final static String WORDPRESS_POST_TITLE = "title";
    final static String WORDPRESS_POST_URL = "URL";
    final static String WORDPRESS_POST_IMAGE = "featured_image";
    final static String WORDPRESS_POST_CONTENT = "content";

    /*
     * Retorna os campos JSON referente a um post do WordPress. Útil para filtrar a String JSON
     * retornada pelo servidor na classe WordPressJson por apenas estes campos.
     */
    public static String getJsonHistoryFields(){
        return WORDPRESS_POST_ID + "," +
                WORDPRESS_POST_DATE + "," +
                WORDPRESS_POST_MODIFIED + "," +
                WORDPRESS_POST_TITLE + "," +
                WORDPRESS_POST_URL + "," +
                WORDPRESS_POST_IMAGE + "," +
                WORDPRESS_POST_CONTENT;
    }

    public static List<History> getHistoriesFromJson(String historiesJsonStr)
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

        List<History> histories = new ArrayList<>();

        JSONArray historyArray = baseJsonResponse.getJSONArray(WORDPRESS_RESULTS);

        for (int i = 0; i < historyArray.length(); i++) {
            JSONObject currentHistory = historyArray.getJSONObject(i);
            //ToDo: Pegar ID, date e modified
            String title = currentHistory.getString(WORDPRESS_POST_TITLE);
            String urlHistory = currentHistory.getString(WORDPRESS_POST_URL);
            String urlImage = currentHistory.getString(WORDPRESS_POST_IMAGE);
            String content = currentHistory.getString(WORDPRESS_POST_CONTENT);

            History history = new History(title, urlHistory, urlImage, content);
            histories.add(history);
        }

        return histories;
    }
}