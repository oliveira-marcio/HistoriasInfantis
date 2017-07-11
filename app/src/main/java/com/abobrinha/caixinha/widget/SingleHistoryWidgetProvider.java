package com.abobrinha.caixinha.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.abobrinha.caixinha.data.PreferencesUtils;
import com.abobrinha.caixinha.sync.NotificationUtils;


public class SingleHistoryWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent intent = new Intent(context, SingleHistoryIntentService.class);
        intent.setAction(SingleHistoryIntentService.ACTION_UPDATE_ALL_WIDGETS);
        context.startService(intent);
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
            Intent updateIntent = new Intent(context, SingleHistoryIntentService.class);
            updateIntent.setAction(SingleHistoryIntentService.ACTION_UPDATE_ALL_WIDGETS);
            context.startService(updateIntent);
        }
    }
}