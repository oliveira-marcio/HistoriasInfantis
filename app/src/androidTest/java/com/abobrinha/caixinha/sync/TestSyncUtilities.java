package com.abobrinha.caixinha.sync;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.abobrinha.caixinha.network.WordPressConn;
import com.abobrinha.caixinha.network.WordPressJson;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import static com.abobrinha.caixinha.network.WordPressConn.PAGE_PARAM;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;


public class TestSyncUtilities {

    static URL buildUrl(String urlString, int results_per_page, int page) {
        Uri builtUri = Uri.parse(urlString).buildUpon()
                .appendPath(WordPressConn.WORDPRESS_ABOBRINHA_ID)
                .appendPath(WordPressConn.WORDPRESS_POSTS)
                .appendQueryParameter(WordPressConn.CATEGORY_PARAM, WordPressConn.CATEGORY_VALUE)
                .appendQueryParameter(WordPressConn.NUMBER_PARAM, Integer.toString(results_per_page))
                .appendQueryParameter(PAGE_PARAM, Integer.toString(page))
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    static String fetchDataFromUrl(URL url) throws IOException {
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

    static List<ContentValues> getDataFromAllApiPages(Context context, String urlString,
                                                      int results_per_page)
            throws IOException, JSONException {
        List<ContentValues> dataRetrieved = new ArrayList<>();
        for (int page = 1; page <= 99999; page++) {
            URL url = buildUrl(urlString, results_per_page, page);
            String searchResults = TestSyncUtilities.fetchDataFromUrl(url);
            if (searchResults == null) {
                return null;
            }

            List<ContentValues> data = WordPressJson.getHistoriesFromJson(context, searchResults);
            if (data == null) break;

            dataRetrieved.addAll(data);
        }

        if (dataRetrieved.size() == 0) {
            return null;
        }

        return dataRetrieved;
    }

    static void validateCurrentRecord(String error, ContentValues actualValues, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> expectedValueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : expectedValueSet) {
            String expectedKey = entry.getKey();
            String actualValue = actualValues.getAsString(expectedKey);

            String keyNotFoundError = "Chave '" + expectedKey + "' não encontrada. " + error;
            assertFalse(keyNotFoundError, actualValue == null);

            String expectedValue = entry.getValue().toString();

            String valuesDontMatchError = "[Chave '" + expectedKey + "'] Valor atual '" + actualValue
                    + "' não bate com o valor esperado '" + expectedValue + "'. "
                    + error;

            assertEquals(valuesDontMatchError,
                    expectedValue,
                    actualValue);
        }
    }
}
