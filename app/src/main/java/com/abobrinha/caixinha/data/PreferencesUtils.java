package com.abobrinha.caixinha.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.widget.SingleHistoryConfigureAdapter;

public class PreferencesUtils {
    private PreferencesUtils() {
    }

    public static final int CATEGORY_HISTORIES = 0;
    public static final int CATEGORY_FAVORITES = 1;

    public static final int ORDER_DATE = 0;
    public static final int ORDER_TITLE = 1;

    public static final int HISTORY_STATUS_OK = 0;
    public static final int HISTORY_STATUS_SERVER_DOWN = 1;
    public static final int HISTORY_STATUS_SERVER_INVALID = 2;
    public static final int HISTORY_STATUS_UNKNOWN = 3;

    private static final String SINGLE_WIDGET_PREF_PREFIX_KEY = "single_widget_";
    private static final String LIST_WIDGET_PREF_PREFIX_KEY = "list_widget_";

    private static final String CATEGORY_PREFIX_KEY = "category";
    private static final String ORDER_PREFIX_KEY = "order_";


    public static void setHistoryStatus(Context c, int historyStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_history_status_key), historyStatus);
        spe.apply();
    }

    public static int getHistoryStatus(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_history_status_key), HISTORY_STATUS_UNKNOWN);
    }

    public static void setMainHistoryCategory(Context c, int category) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_history_category_key), category);
        spe.apply();
    }

    public static int getMainHistoryCategory(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_history_category_key), CATEGORY_HISTORIES);
    }

    public static String getGridHistoryOrder(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        String orderByPreference = sp.getString(
                c.getString(R.string.pref_order_key),
                c.getString(R.string.pref_order_date)
        );

        return orderByPreference.equals(c.getString(R.string.pref_order_title))
                ? HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE + " ASC"
                : HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE + " DESC";
    }

    public static boolean areNotificationsEnabled(Context context) {
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean shouldDisplayNotificationsByDefault = context
                .getResources()
                .getBoolean(R.bool.show_notifications_by_default);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean shouldDisplayNotifications = sp
                .getBoolean(displayNotificationsKey, shouldDisplayNotificationsByDefault);

        return shouldDisplayNotifications;
    }

    public static void saveWidgetHistoryPref(Context c, int appWidgetId, long historyId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putLong(SINGLE_WIDGET_PREF_PREFIX_KEY + appWidgetId, historyId);
        spe.apply();
    }

    public static long loadWidgetHistoryPref(Context c, int appWidgetId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getLong(SINGLE_WIDGET_PREF_PREFIX_KEY + appWidgetId,
                SingleHistoryConfigureAdapter.INVALID_HISTORY_ID);
    }

    public static void deleteWidgetHistoryPref(Context c, int appWidgetId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.remove(SINGLE_WIDGET_PREF_PREFIX_KEY + appWidgetId);
        spe.apply();
    }

    public static void saveWidgetCategoryPref(Context c, int appWidgetId, int category) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(LIST_WIDGET_PREF_PREFIX_KEY + CATEGORY_PREFIX_KEY + appWidgetId, category);
        spe.apply();
    }

    public static int loadWidgetCategoryPref(Context c, int appWidgetId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(LIST_WIDGET_PREF_PREFIX_KEY + CATEGORY_PREFIX_KEY + appWidgetId,
                CATEGORY_HISTORIES);
    }

    public static void deleteWidgetCategoryPref(Context c, int appWidgetId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.remove(LIST_WIDGET_PREF_PREFIX_KEY + CATEGORY_PREFIX_KEY + appWidgetId);
        spe.apply();
    }

    public static void saveWidgetOrderPref(Context c, int appWidgetId, int category) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();

        spe.putInt(
                LIST_WIDGET_PREF_PREFIX_KEY + ORDER_PREFIX_KEY + appWidgetId,
                category == ORDER_TITLE ? ORDER_TITLE : ORDER_DATE
        );
        spe.apply();
    }

    public static int loadWidgetOrderPref(Context c, int appWidgetId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(LIST_WIDGET_PREF_PREFIX_KEY + ORDER_PREFIX_KEY + appWidgetId, ORDER_DATE);

    }

    public static String getDatabaseOrderByPref(Context c, int appWidgetId) {
        return loadWidgetOrderPref(c, appWidgetId) == ORDER_TITLE
                ? HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE + " ASC"
                : HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE + " DESC";
    }

    public static void deleteWidgetOrderPref(Context c, int appWidgetId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.remove(LIST_WIDGET_PREF_PREFIX_KEY + ORDER_PREFIX_KEY + appWidgetId);
        spe.apply();
    }

}
