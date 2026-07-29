package com.ft.sdk.internal.anr.historical;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ApplicationExitInfo;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.R)
final class AndroidProcessExitSource implements ProcessExitSource {
    private static final int MAX_EXIT_RECORDS = 64;

    private final ActivityManager activityManager;

    AndroidProcessExitSource(Context context) {
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    @SuppressLint("NewApi")
    @Override
    public List<ProcessExitRecord> load() {
        ArrayList<ProcessExitRecord> records = new ArrayList<>();
        if (activityManager == null) {
            return records;
        }
        List<ApplicationExitInfo> exits =
                activityManager.getHistoricalProcessExitReasons(null, 0, MAX_EXIT_RECORDS);
        if (exits == null) {
            return records;
        }
        for (final ApplicationExitInfo exit : exits) {
            records.add(new ProcessExitRecord(
                    exit.getProcessName(),
                    exit.getPid(),
                    exit.getTimestamp(),
                    exit.getReason(),
                    exit.getImportance(),
                    new ProcessExitRecord.TraceSource() {
                        @Override
                        public InputStream open() throws IOException {
                            return exit.getTraceInputStream();
                        }
                    }));
        }
        return records;
    }
}
