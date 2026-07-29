package com.ft.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.service.HistoricalAnrWorkerService;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 30)
public class HistoricalAnrMultiProcessTest {
    @Test
    public void workerInitializesSdkAndPersistsItsOwnViewIdentity() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        CountDownLatch ready = new CountDownLatch(1);
        final boolean[] installed = {false};
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ignored, Intent intent) {
                installed[0] = intent.getBooleanExtra(
                        HistoricalAnrWorkerService.EXTRA_SDK_INSTALLED, false);
                ready.countDown();
            }
        };
        registerReceiver(context, receiver);
        try {
            context.startService(HistoricalAnrWorkerService.prepareIntent(context));
            assertTrue("worker did not become ready", ready.await(10, TimeUnit.SECONDS));
            assertTrue("SDK was not initialized in :anr_worker", installed[0]);

            ViewBean workerView = waitForWorkerView();
            assertNotNull("worker View was not persisted", workerView);
            assertTrue(workerView.getProcessName().endsWith(":anr_worker"));
            assertNotNull(workerView.getProcessRunId());
        } finally {
            context.unregisterReceiver(receiver);
            context.stopService(HistoricalAnrWorkerService.prepareIntent(context));
        }
    }

    private ViewBean waitForWorkerView() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10_000;
        do {
            List<ViewBean> views = FTDBManager.get().querySumView(0, true);
            for (ViewBean view : views) {
                if (view.getProcessName() != null
                        && view.getProcessName().endsWith(":anr_worker")) {
                    return view;
                }
            }
            Thread.sleep(100);
        } while (System.currentTimeMillis() < deadline);
        return null;
    }

    private void registerReceiver(Context context, BroadcastReceiver receiver) {
        IntentFilter filter = new IntentFilter(HistoricalAnrWorkerService.ACTION_READY);
        if (Build.VERSION.SDK_INT >= 33) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(receiver, filter);
        }
    }
}
