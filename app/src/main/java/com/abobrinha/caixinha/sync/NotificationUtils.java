package com.abobrinha.caixinha.sync;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;
import com.abobrinha.caixinha.ui.HistoryActivity;
import com.abobrinha.caixinha.ui.MainActivity;
import com.bumptech.glide.Glide;

public class NotificationUtils {

    private static final int HISTORY_NOTIFICATION_ID = 1234;

    /**
     * Cria notificações de acordo com a quantidade de novas histórias adicionadas.
     * Caso seja 1, mostra o título e a imagem da mesma e o clique leva diretamente para a
     * leitura da história. Caso sejam mais, mostra o total de novas histórias e o clique leva
     * para a tela principal.
     *
     * @param context
     * @param quantity é a quantidade de novas histórias adicionadas.
     */
    public static void notifyUserOfNewHistories(Context context, int quantity) {
        String notificationTitle, notificationText;
        Intent historyIntent;
        Bitmap largeIcon;
        Resources resources = context.getResources();

        if (quantity > 1) {
            largeIcon = BitmapFactory.decodeResource(
                    resources,
                    R.mipmap.ic_launcher);

            notificationTitle = context.getString(R.string.app_name);
            notificationText =
                    String.format(context.getString(R.string.notification_new_histories), quantity);

            historyIntent = new Intent(context, MainActivity.class);
            historyIntent.setData(HistoryContract.HistoriesEntry.CONTENT_URI);

            makeNotification(context, notificationTitle, notificationText, largeIcon, historyIntent);
        } else if (quantity == 1) {
            Cursor cursor = context.getContentResolver().query(
                    HistoryContract.HistoriesEntry.CONTENT_URI,
                    MainActivity.MAIN_HISTORIES_PROJECTION,
                    null,
                    null,
                    HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE + " DESC"
            );

            if (cursor.moveToFirst()) {
                Uri historyUri = HistoryContract.HistoriesEntry
                        .buildSingleHistoryUri(cursor.getLong(MainActivity.INDEX_HISTORY_ID));
                String historyTitle = cursor.getString(MainActivity.INDEX_HISTORY_TITLE);
                String imageUrl = cursor.getString(MainActivity.INDEX_HISTORY_IMAGE);

                try {
                    largeIcon = Glide.with(context).load(imageUrl).asBitmap().into(-1, -1).get();
                } catch (Exception e) {
                    largeIcon = BitmapFactory.decodeResource(resources, R.drawable.sobre);
                }

                notificationTitle = context.getString(R.string.app_name) + " - " +
                        context.getString(R.string.notification_new_history);
                notificationText = historyTitle;

                historyIntent = new Intent(context, HistoryActivity.class);
                historyIntent.setData(historyUri);

                makeNotification(context, notificationTitle, notificationText, largeIcon, historyIntent);
            }

            cursor.close();
        }
    }

    private static void makeNotification(Context context, String notificationTitle,
                                         String notificationText, Bitmap largeIcon,
                                         Intent historyIntent) {
        int smallArtResourceId = R.drawable.ic_about;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(smallArtResourceId)
                .setLargeIcon(largeIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setAutoCancel(true);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(historyIntent);
        PendingIntent resultPendingIntent = taskStackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(HISTORY_NOTIFICATION_ID, notificationBuilder.build());
    }
}
