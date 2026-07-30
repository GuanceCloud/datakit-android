package com.ft;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Debug-only JobService used to cold-start the process without an Activity.
 */
public class BackgroundLaunchJobService extends JobService {
    public static final int JOB_ID = 4242;
    private static final String TAG = "BackgroundLaunchJob";
    private static final long KEEP_ALIVE_MS = TimeUnit.MINUTES.toMillis(2);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private JobParameters runningParams;

    public static void schedule(Context context) {
        JobScheduler scheduler =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null) {
            Log.e(TAG, "JobScheduler unavailable");
            return;
        }
        JobInfo job = new JobInfo.Builder(
                JOB_ID,
                new ComponentName(context, BackgroundLaunchJobService.class))
                .setMinimumLatency(TimeUnit.HOURS.toMillis(1))
                .setOverrideDeadline(TimeUnit.HOURS.toMillis(2))
                .build();
        Log.d(TAG, "schedule result=" + scheduler.schedule(job));
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        runningParams = params;
        ActivityManager.RunningAppProcessInfo processInfo =
                new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(processInfo);
        Log.d(TAG, "job launch, importance=" + processInfo.importance);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (runningParams != null) {
                    jobFinished(runningParams, false);
                    runningParams = null;
                }
            }
        }, KEEP_ALIVE_MS);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        handler.removeCallbacksAndMessages(null);
        runningParams = null;
        return false;
    }
}
