package com.abobrinha.caixinha.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.PreferencesUtils;
import com.abobrinha.caixinha.ui.HistoryActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;


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
            SingleHistoryConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }
}

