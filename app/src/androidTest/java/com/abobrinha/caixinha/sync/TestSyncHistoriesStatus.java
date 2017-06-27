package com.abobrinha.caixinha.sync;


import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.abobrinha.caixinha.network.WordPressConn;
import com.abobrinha.caixinha.network.WordPressJson;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import static junit.framework.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class TestSyncHistoriesStatus {
    private final Context context = InstrumentationRegistry.getTargetContext();

    private final String EMPTY_URL = "http://google.com/?";
    private final String INVALID_URL = "http://google.com/ping?";
    private final String VALID_URL = WordPressConn.WORDPRESS_BASE_URL;

    @Test
    public void testServerStatus() {
        String error = "Erro no teste de servidor fora do ar";
        assertEquals(error, HistorySyncTask.HISTORY_STATUS_SERVER_DOWN, getResultFromUrl(EMPTY_URL));

        error = "Erro no teste de dados inválidos do servidor";
        assertEquals(error, HistorySyncTask.HISTORY_STATUS_SERVER_INVALID, getResultFromUrl(INVALID_URL));

        error = "Erro no teste de dados válidos do servidor";
        assertEquals(error, HistorySyncTask.HISTORY_STATUS_OK, getResultFromUrl(VALID_URL));
    }

    private int getResultFromUrl(String url) {
        try {
            String searchResults = fetchDataFromUrl(buildUrl(url));
            if (searchResults == null) {
                return HistorySyncTask.HISTORY_STATUS_SERVER_DOWN;
            }
            WordPressJson.getHistoriesFromJson(context, searchResults);
            return HistorySyncTask.HISTORY_STATUS_OK;
        } catch (IOException e) {
            return HistorySyncTask.HISTORY_STATUS_SERVER_DOWN;
        } catch (JSONException e) {
            return HistorySyncTask.HISTORY_STATUS_SERVER_INVALID;
        }
    }

    private URL buildUrl(String urlString) {
        Uri builtUri = Uri.parse(urlString).buildUpon()
                .appendPath(WordPressConn.WORDPRESS_ABOBRINHA_ID)
                .appendPath(WordPressConn.WORDPRESS_POSTS)
                .appendQueryParameter(WordPressConn.CATEGORY_PARAM, WordPressConn.CATEGORY_VALUE)
                .appendQueryParameter(WordPressConn.NUMBER_PARAM, "1")
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    private String fetchDataFromUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
