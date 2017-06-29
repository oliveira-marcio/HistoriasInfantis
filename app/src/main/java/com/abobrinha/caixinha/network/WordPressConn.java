package com.abobrinha.caixinha.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class WordPressConn {

    public final static String WORDPRESS_BASE_URL = "https://public-api.wordpress.com/rest/v1.1/sites/";
    public final static String WORDPRESS_ABOBRINHA_ID = "113100833";
    public final static String WORDPRESS_POSTS = "posts";

    public final static String FIELDS_PARAM = "fields";
    public final static String CATEGORY_PARAM = "category";
    public final static String NUMBER_PARAM = "number";
    public final static String PAGE_PARAM = "page";

    public final static String CATEGORY_VALUE = "historias-infantis-abobrinha";

    private WordPressConn() {
    }

    /**
     * Contrói a URL para consultar o WordPress API
     */
    private static URL buildUrl(int results_per_page, int page) {
        Uri builtUri = Uri.parse(WORDPRESS_BASE_URL).buildUpon()
                .appendPath(WORDPRESS_ABOBRINHA_ID)
                .appendPath(WORDPRESS_POSTS)
                .appendQueryParameter(FIELDS_PARAM, WordPressJson.getJsonHistoryFields())
                .appendQueryParameter(CATEGORY_PARAM, CATEGORY_VALUE)
                .appendQueryParameter(NUMBER_PARAM, Integer.toString(results_per_page))
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

    /**
     * Retorna a string JSON com o resultado de uma página específica da consulta ao WordPress.
     */
    public static String getResponseFromApiPage(int results_per_page, int page) throws IOException {
        URL url = buildUrl(results_per_page, page);
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

    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}