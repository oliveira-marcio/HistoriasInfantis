package com.abobrinha.caixinha.widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.TaskStackBuilder;
import android.view.View;
import android.widget.RemoteViews;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;
import com.abobrinha.caixinha.data.PreferencesUtils;
import com.abobrinha.caixinha.ui.HistoryActivity;
import com.abobrinha.caixinha.ui.MainActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;


public class SingleHistoryJobIntentService extends JobIntentService {

    public static final String ACTION_UPDATE_SINGLE_WIDGET = "update-single-widget";
    public static final String ACTION_UPDATE_ALL_WIDGETS = "update-all-widgets";

    static final int JOB_ID = 1000;

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SingleHistoryJobIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
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

        long historyId = PreferencesUtils.loadWidgetHistoryPref(this, appWidgetId);
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

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.single_history_widget);
        Intent historyIntent;

        if (cursor != null && cursor.moveToNext()) {
            for (int i = 0; i < historyData.length; i++) {
                historyData[i] = cursor.getString(i);
            }
            cursor.close();

            views.setTextViewText(R.id.appwidget_title, historyData[1]);
            views.setViewVisibility(R.id.appwidget_title, View.VISIBLE);
            views.setViewVisibility(R.id.appwidget_error, View.GONE);

            Bitmap image;
            try {
                image = Glide.with(getApplicationContext())
                        .load(historyData[2])
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.img_about)
                        .error(R.drawable.img_about)
                        .into(-1, -1)
                        .get();
            } catch (Exception e) {
                image = BitmapFactory.decodeResource(getResources(), R.drawable.img_about);
            }
            views.setImageViewBitmap(R.id.appwidget_background, image);
            views.setContentDescription(R.id.appwidget_background, historyData[1]);

            historyIntent = new Intent(this, HistoryActivity.class);
            historyIntent.putExtra(Intent.EXTRA_TEXT, Long.valueOf(historyData[0]));
            historyIntent.putExtra(getString(R.string.notification_intent),
                    PreferencesUtils.CATEGORY_HISTORIES);

        } else {
            PreferencesUtils.deleteWidgetHistoryPref(this, appWidgetId);

            views.setTextViewText(R.id.appwidget_error, getString(R.string.single_widget_error));
            views.setViewVisibility(R.id.appwidget_title, View.GONE);
            views.setViewVisibility(R.id.appwidget_error, View.VISIBLE);

            views.setImageViewResource(R.id.appwidget_background, R.drawable.img_about);
            views.setContentDescription(R.id.appwidget_background, getString(R.string.single_widget_error));

            historyIntent = new Intent(this, MainActivity.class);
        }

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
