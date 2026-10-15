package com.ft.launchfixture;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.LaunchAcceptanceProbe;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class LaunchControlProvider extends ContentProvider {
    @Override public boolean onCreate() { return true; }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        FutureTask<Bundle> task = new FutureTask<>(() -> {
            if ("release".equals(method)) {
                LaunchActivity.releaseFirstDraw();
            } else if ("repeat_hook".equals(method)) {
                FTAutoTrack.startApp((android.app.Application) getContext().getApplicationContext());
                LaunchApplication.event("repeat_hook");
            } else if ("finish_background_for_test".equals(method)) {
                // A headless launch has no automatic first-draw endpoint. This explicit
                // test endpoint validates serialization without pretending a frame drew.
                LaunchAcceptanceProbe.finishBackgroundForTest();
                LaunchApplication.event("explicit_background_test_completion");
            } else if (!"status".equals(method)) {
                throw new IllegalArgumentException(method);
            }
            JSONObject report = LaunchAcceptanceProbe.snapshot();
            report.put("events", LaunchApplication.EVENTS);
            File file = new File(getContext().getFilesDir(), "launch-acceptance.json");
            try (FileOutputStream output = new FileOutputStream(file)) {
                output.write(report.toString().getBytes(StandardCharsets.UTF_8));
            }
            Bundle result = new Bundle();
            result.putString("report", file.getName());
            return result;
        });
        if (Looper.myLooper() == Looper.getMainLooper()) task.run();
        else new Handler(Looper.getMainLooper()).post(task);
        try {
            return task.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override public Cursor query(Uri u, String[] p, String s, String[] a, String o) { return null; }
    @Override public String getType(Uri u) { return null; }
    @Override public Uri insert(Uri u, ContentValues v) { return null; }
    @Override public int delete(Uri u, String s, String[] a) { return 0; }
    @Override public int update(Uri u, ContentValues v, String s, String[] a) { return 0; }
}
