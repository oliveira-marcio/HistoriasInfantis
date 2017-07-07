package com.abobrinha.caixinha.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;
import com.bumptech.glide.Glide;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SingleHistoryConfigureActivity SingleHistoryConfigureActivity}
 */
public class SingleHistoryWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

//        CharSequence widgetText = "" + SingleHistoryConfigureActivity.loadHistoryPref(context, appWidgetId);
        String[] historyData = getHistoryTitle(context, appWidgetId);
        if (historyData == null) return;

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.single_history_widget);
        views.setTextViewText(R.id.appwidget_text, historyData[1]);

        Bitmap image;
        try {
            image = Glide.with(context)
                    .load(historyData[2])
                    .asBitmap()
                    .placeholder(R.drawable.capa)
                    .into(-1, -1)
                    .get();
        } catch (Exception e) {
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.capa);
        }
        views.setImageViewBitmap(R.id.appwidget_background, image);

        Intent intent = new Intent(context, SingleHistoryConfigureActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.appwidget_button, pendingIntent);

        // Instruct the widget manager to update the widget
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

