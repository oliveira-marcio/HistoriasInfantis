package com.abobrinha.caixinha.sync;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;


public class HistoryFirebaseJobService extends JobService {

    private AsyncTask<Void, Void, Void> mFetchHistoriesTask;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        mFetchHistoriesTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Context context = getApplicationContext();
                HistorySyncTask.syncHistories(context);
                jobFinished(jobParameters, false);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(jobParameters, false);
            }
        };

        mFetchHistoriesTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mFetchHistoriesTask != null) {
            mFetchHistoriesTask.cancel(true);
        }
        return true;
    }
}
