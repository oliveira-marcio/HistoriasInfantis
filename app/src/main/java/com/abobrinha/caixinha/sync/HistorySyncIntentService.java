package com.abobrinha.caixinha.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;


public class HistorySyncIntentService extends IntentService {

    public HistorySyncIntentService() {
        super("HistorySyncIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        HistorySyncTask.syncHistories(this);
    }
}
