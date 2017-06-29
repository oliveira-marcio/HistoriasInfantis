package com.abobrinha.caixinha.network;

import android.content.ContentValues;
import android.content.Context;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class WordPressUtils {
    public static final int RESULTS_PER_PAGE = 100;

    private WordPressUtils() {
    }

    public static ContentValues[] getDataFromAllApiPages(Context context)
            throws IOException, JSONException {
        List<ContentValues> dataRetrieved = new ArrayList<>();
        for (int page = 1; page <= 99999; page++) {
            String searchResults = WordPressConn.getResponseFromApiPage(RESULTS_PER_PAGE, page);
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

        return dataRetrieved.toArray(new ContentValues[0]);
    }
}
