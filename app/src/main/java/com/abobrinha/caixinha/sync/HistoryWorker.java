package com.abobrinha.caixinha.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class HistoryWorker extends Worker {

    public HistoryWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        HistorySyncTask.syncHistories(context);
        return Result.success();
    }
}