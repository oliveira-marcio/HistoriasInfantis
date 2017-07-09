package com.abobrinha.caixinha.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;
import com.abobrinha.caixinha.data.PreferencesUtils;
import com.abobrinha.caixinha.ui.HistoryActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SingleHistoryConfigureActivity SingleHistoryConfigureActivity}
 */
public class SingleHistoryWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
// ToDo: passar a obtenção dos dados e atualização do widget para um IntentService
// ToDo: tratar remoção de histórias
        String[] historyData = getHistoryTitle(context, appWidgetId);
        if (historyData == null) return;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.single_history_widget);
        views.setTextViewText(R.id.appwidget_text, historyData[1]);

        AppWidgetTarget appWidgetTarget = new AppWidgetTarget(
                context, views, R.id.appwidget_background, appWidgetId);

        Glide.with(context.getApplicationContext())
                .load(historyData[2])
                .asBitmap()
                .placeholder(R.drawable.img_about)
                .error(R.drawable.img_about)
                .into(appWidgetTarget);

        Intent historyIntent = new Intent(context, HistoryActivity.class);
        historyIntent.putExtra(Intent.EXTRA_TEXT, Long.valueOf(historyData[0]));
        historyIntent.putExtra(context.getString(R.string.notification_intent),
                PreferencesUtils.CATEGORY_HISTORIES);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(historyIntent);
        PendingIntent pendingHistoryIntent = taskStackBuilder
                .getPendingIntent(appWidgetId, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.root_view, pendingHistoryIntent);

        Intent configIntent = new Intent(context, SingleHistoryConfigureActivity.class);
//        configIntent.setAction("WIDGET_CONFIGURED");
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        PendingIntent pendingConfigIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent pendingConfigIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_button, pendingConfigIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static String[] getHistoryTitle(Context context, int appWidgetId) {
        long historyId = SingleHistoryConfigureActivity.loadHistoryPref(context, appWidgetId);
        if (historyId == SingleHistoryConfigureAdapter.INVALID_HISTORY_ID) return null;

        final String[] projection = {
                HistoryContract.HistoriesEntry._ID,
                HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE,
                HistoryContract.HistoriesEntry.COLUMN_HISTORY_IMAGE
        };

        Cursor cursor = context.getContentResolver().query(
                HistoryContract.HistoriesEntry.buildSingleHistoryUri(historyId),
                projection,
                null,
                null,
                null);

        String[] historyData = new String[projection.length];

        if (cursor != null & cursor.moveToNext()) {
            for (int i = 0; i < historyData.length; i++) {
                historyData[i] = cursor.getString(i);
            }
        }

        cursor.close();
        return historyData;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            SingleHistoryConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

