package com.abobrinha.caixinha.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.abobrinha.caixinha.R;

public class PreferencesUtils {
    private PreferencesUtils() {
    }

    public static final int HISTORIES_CATEGORY_INDEX = 0;
    public static final int FAVORITES_CATEGORY_INDEX = 1;

    public static final int HISTORY_STATUS_OK = 0;
    public static final int HISTORY_STATUS_SERVER_DOWN = 1;
    public static final int HISTORY_STATUS_SERVER_INVALID = 2;
    public static final int HISTORY_STATUS_UNKNOWN = 3;

    public static final String HISTORY_CATEGORY_KEY = "history_category";
    public static final String HISTORY_STATUS_KEY = "history_status";

    public static void setHistoryStatus(Context c, int historyStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(HISTORY_STATUS_KEY, historyStatus);
        spe.apply();
    }

    public static int getHistoryStatus(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(HISTORY_STATUS_KEY, HISTORY_STATUS_UNKNOWN);
    }

    public static void setMainHistoryCategory(Context c, int category) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(HISTORY_CATEGORY_KEY, category);
        spe.apply();
    }

    public static int getMainHistoryCategory(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(HISTORY_CATEGORY_KEY, HISTORIES_CATEGORY_INDEX);
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
}
