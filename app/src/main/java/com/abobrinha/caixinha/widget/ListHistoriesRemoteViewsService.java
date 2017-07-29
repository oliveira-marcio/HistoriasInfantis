package com.abobrinha.caixinha.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;
import com.abobrinha.caixinha.data.PreferencesUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;


public class ListHistoriesRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        Integer appWidgetId = Integer.valueOf(intent.getData().getSchemeSpecificPart());

        if (appWidgetId == null || appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return null;
        }
        return new ListHistoriesRemoteViewsFactory(this.getApplicationContext(), appWidgetId);
    }
}

class ListHistoriesRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String[] MAIN_HISTORIES_PROJECTION = {
            HistoryContract.HistoriesEntry._ID,
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_TITLE,
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_IMAGE,
            HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE
    };

    private static final int INDEX_HISTORY_ID = 0;
    private static final int INDEX_HISTORY_TITLE = 1;
    private static final int INDEX_HISTORY_IMAGE = 2;

    private final Uri[] mCategoryUri = new Uri[]{
            HistoryContract.HistoriesEntry.CONTENT_URI,
            HistoryContract.HistoriesEntry.buildFavoritesUri()
    };

    private Context mContext;
    private Cursor mCursor = null;
    private int mAppWidgetId;

    public ListHistoriesRemoteViewsFactory(Context applicationContext, int appWidgetId) {
        mContext = applicationContext;
        mAppWidgetId = appWidgetId;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }

        int category = PreferencesUtils.loadWidgetCategoryPref(mContext, mAppWidgetId);

        final long identityToken = Binder.clearCallingIdentity();

        mCursor = mContext.getContentResolver().query(mCategoryUri[category],
                MAIN_HISTORIES_PROJECTION,
                null,
                null,
                PreferencesUtils.getDatabaseOrderByPref(mContext, mAppWidgetId));

        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                mCursor == null || !mCursor.moveToPosition(position)) {
            return null;
        }

        RemoteViews views = new RemoteViews(mContext.getPackageName(),
                R.layout.list_history_widget_item);

        Bitmap image;
        try {
            image = Glide.with(mContext.getApplicationContext())
                    .load(mCursor.getString(INDEX_HISTORY_IMAGE))
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.img_about)
                    .error(R.drawable.img_about)
                    .into(-1, -1)
                    .get();
        } catch (Exception e) {
            image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.img_about);
        }
        views.setImageViewBitmap(R.id.history_image, image);
        views.setContentDescription(R.id.history_image, mCursor.getString(INDEX_HISTORY_TITLE));

        views.setTextViewText(R.id.history_title, mCursor.getString(INDEX_HISTORY_TITLE));

        final Intent fillInIntent = new Intent();
        fillInIntent.putExtra(Intent.EXTRA_TEXT, mCursor.getLong(INDEX_HISTORY_ID));
        views.setOnClickFillInIntent(R.id.list_item, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}