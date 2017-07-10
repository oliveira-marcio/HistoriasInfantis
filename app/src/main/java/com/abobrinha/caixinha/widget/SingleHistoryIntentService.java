package com.abobrinha.caixinha.widget;


import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;
import com.abobrinha.caixinha.data.PreferencesUtils;
import com.abobrinha.caixinha.ui.HistoryActivity;
import com.bumptech.glide.Glide;


public class SingleHistoryIntentService extends IntentService {

    public static final String ACTION_UPDATE_SINGLE_WIDGET = "update-single-widget";
    public static final String ACTION_UPDATE_ALL_WIDGETS = "update-all-widgets";

    public SingleHistoryIntentService() {
        super("SingleHistoryIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        String action = intent.getAction();

        switch (action) {
            case ACTION_UPDATE_SINGLE_WIDGET:
                updateWidget(appWidgetManager,
                        intent.getIntExtra(
                                AppWidgetManager.EXTRA_APPWIDGET_ID,
                                AppWidgetManager.INVALID_APPWIDGET_ID));
                break;

            case ACTION_UPDATE_ALL_WIDGETS:
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                        SingleHistoryWidgetProvider.class));

                for (int appWidgetId : appWidgetIds) {
                    updateWidget(appWidgetManager, appWidgetId);
                }
                break;
        }
    }

    private void updateWidget(AppWidgetManager appWidgetManager, int appWidgetId) {
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return;

        long historyId = SingleHistoryConfigureActivity.loadHistoryPref(this, appWidgetId);
        if (historyId == SingleHistoryConfigureAdapter.INVALID_HISTORY_ID) return;

        final String[] projection = {
                HistoryContract.HistoriesEntry._ID,
                HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE,
                HistoryContract.HistoriesEntry.COLUMN_HISTORY_IMAGE
        };

        Cursor cursor = getContentResolver().query(
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

// ToDo: tratar remoção de histórias
        if (historyData == null) return;

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.single_history_widget);
        views.setTextViewText(R.id.appwidget_text, historyData[1]);

        Bitmap image;
        try {
            image = Glide.with(getApplicationContext())
                    .load(historyData[2])
                    .asBitmap()
                    .placeholder(R.drawable.img_about)
                    .error(R.drawable.img_about)
                    .into(-1, -1)
                    .get();
        } catch (Exception e) {
            image = BitmapFactory.decodeResource(getResources(), R.drawable.img_about);
        }
        views.setImageViewBitmap(R.id.appwidget_background, image);

        Intent historyIntent = new Intent(this, HistoryActivity.class);
        historyIntent.putExtra(Intent.EXTRA_TEXT, Long.valueOf(historyData[0]));
        historyIntent.putExtra(getString(R.string.notification_intent),
                PreferencesUtils.CATEGORY_HISTORIES);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addNextIntentWithParentStack(historyIntent);
        PendingIntent pendingHistoryIntent = taskStackBuilder
                .getPendingIntent(appWidgetId, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.root_view, pendingHistoryIntent);

        Intent configIntent = new Intent(this, SingleHistoryConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        PendingIntent pendingConfigIntent = PendingIntent.getActivity(this, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.appwidget_button, pendingConfigIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
