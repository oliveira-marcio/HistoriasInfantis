package com.abobrinha.caixinha.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.abobrinha.caixinha.data.PreferencesUtils;
import com.abobrinha.caixinha.sync.NotificationUtils;


public class SingleHistoryWidgetProvider extends AppWidgetProvider {

    private final String LOG_TAG = SingleHistoryWidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent intent = new Intent(context, SingleHistoryJobIntentService.class);
        intent.setAction(SingleHistoryJobIntentService.ACTION_UPDATE_ALL_WIDGETS);
        SingleHistoryJobIntentService.enqueueWork(context, SingleHistoryJobIntentService.class,
                SingleHistoryJobIntentService.JOB_ID, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            PreferencesUtils.deleteWidgetHistoryPref(context, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (NotificationUtils.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            Log.i(LOG_TAG, "Atualizando widgets...");
            Intent updateIntent = new Intent(context, SingleHistoryJobIntentService.class);
            updateIntent.setAction(SingleHistoryJobIntentService.ACTION_UPDATE_ALL_WIDGETS);
            SingleHistoryJobIntentService.enqueueWork(context, SingleHistoryJobIntentService.class,
                    SingleHistoryJobIntentService.JOB_ID, updateIntent);
        }
    }
}