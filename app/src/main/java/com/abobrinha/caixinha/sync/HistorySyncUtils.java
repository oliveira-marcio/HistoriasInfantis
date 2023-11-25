package com.abobrinha.caixinha.sync;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.abobrinha.caixinha.data.HistoryContract;

import java.util.concurrent.TimeUnit;

public class HistorySyncUtils {
    private static final int SYNC_INTERVAL_HOURS = 8;
    private static final String HISTORY_SYNC_TAG = "history-sync";
    private static boolean sInitialized;

    private static void scheduleWorkRequest(@NonNull final Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(HistoryWorker.class, SYNC_INTERVAL_HOURS, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(HISTORY_SYNC_TAG, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request);
    }

    /*
     * Inicializa o agendamento da rotina de sincronização da base com a API do Wordpress,
     * se ainda não tiver sido inicializado, e promove uma sincronização imediata da base se
     * a mesma estiver vazia.
     */
    synchronized public static void initialize(@NonNull final Context context) {
        if (sInitialized) return;

        sInitialized = true;
        scheduleWorkRequest(context);

        Thread checkForEmpty = new Thread(new Runnable() {
            @Override
            public void run() {
                Uri uri = HistoryContract.HistoriesEntry.CONTENT_URI;
                String[] projection = new String[]{HistoryContract.HistoriesEntry._ID};

                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

                if (null == cursor || cursor.getCount() == 0) {
                    startImmediateSync(context);
                }

                if (cursor != null) cursor.close();
            }
        });

        checkForEmpty.start();
    }

    public static void startImmediateSync(@NonNull final Context context) {
        Intent intentToSyncImmediately = new Intent(context, HistorySyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}