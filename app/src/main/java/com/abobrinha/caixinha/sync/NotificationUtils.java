package com.abobrinha.caixinha.sync;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.data.HistoryContract;
import com.abobrinha.caixinha.data.PreferencesUtils;
import com.abobrinha.caixinha.ui.HistoryActivity;
import com.abobrinha.caixinha.ui.HistoryGridFragment;
import com.abobrinha.caixinha.ui.MainActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;


public class NotificationUtils {

    private static final int HISTORY_NOTIFICATION_ID = 1234;
    public static final String ACTION_DATA_UPDATED = "com.abobrinha.caixinha.ACTION_DATA_UPDATED";
    public static final String CHANNEL_ID = "com.abobrinha.caixinha.notifications";

    /*
     * Cria notificações de acordo com a quantidade de novas histórias adicionadas.
     * Caso seja 1, mostra o título e a imagem da mesma e o clique leva diretamente para a
     * leitura da história. Caso sejam mais, mostra o total de novas histórias e o clique leva
     * para a tela principal.
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
                    HistoryGridFragment.MAIN_HISTORIES_PROJECTION,
                    null,
                    null,
                    HistoryContract.HistoriesEntry.COLUMN_HISTORY_DATE + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                long historyId = cursor.getLong(HistoryGridFragment.INDEX_HISTORY_ID);
                String historyTitle = cursor.getString(HistoryGridFragment.INDEX_HISTORY_TITLE);
                String imageUrl = cursor.getString(HistoryGridFragment.INDEX_HISTORY_IMAGE);

                try {
                    largeIcon = Glide.with(context.getApplicationContext())
                            .load(imageUrl)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.img_about)
                            .error(R.drawable.img_about)
                            .into(-1, -1)
                            .get();
                } catch (Exception e) {
                    largeIcon = BitmapFactory.decodeResource(resources, R.drawable.img_about);
                }

                notificationTitle = context.getString(R.string.app_name) + " - " +
                        context.getString(R.string.notification_new_history);
                notificationText = historyTitle;

                historyIntent = new Intent(context, HistoryActivity.class);
                historyIntent.putExtra(Intent.EXTRA_TEXT, historyId);
                historyIntent.putExtra(context.getString(R.string.notification_intent),
                        PreferencesUtils.CATEGORY_HISTORIES);

                makeNotification(context, notificationTitle, notificationText, largeIcon, historyIntent);

                cursor.close();
            }
        }
    }

    private static void makeNotification(Context context, String notificationTitle,
                                         String notificationText, Bitmap largeIcon,
                                         Intent historyIntent) {
        int smallArtResourceId = R.drawable.ic_about;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            makeNotificationChannel(context, notificationManager);
        }

        notificationManager.notify(HISTORY_NOTIFICATION_ID, notificationBuilder.build());
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static void makeNotificationChannel(Context context, NotificationManager notificationManager){
        CharSequence name = context.getString(R.string.channel_name);
        String description = context.getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        notificationManager.createNotificationChannel(channel);
    }

    public static void updateWidgets(Context context) {
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}
