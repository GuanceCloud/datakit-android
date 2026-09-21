package com.ft.sdk.sessionreplay;

import android.content.BroadcastReceiver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class BatteryPowerWatcherTest {

    @Test
    public void registrationPreservesActionsAndUsesFlagsAtVersionBoundaries() {
        int originalSdk = Build.VERSION.SDK_INT;
        try {
            // This checks dispatch only, not platform enforcement on newer Android versions.
            for (int sdk : new int[]{21, 25, 26, 32, 33, 34}) {
                ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", sdk);
                RecordingContext context = new RecordingContext();
                BatteryPowerWatcher watcher = new BatteryPowerWatcher();
                watcher.register(context);

                assertSame(watcher, context.receiver);
                assertEquals(2, context.filter.countActions());
                assertTrue(context.filter.hasAction(Intent.ACTION_BATTERY_CHANGED));
                assertTrue(context.filter.hasAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED));
                assertEquals(sdk >= 26 ? Integer.valueOf(0x4) : null, context.flags);
                watcher.unRegister(context);
                assertSame(watcher, context.unregisteredReceiver);
            }
        } finally {
            ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", originalSdk);
        }
    }

    @Test
    // Robolectric 4.7.2 does not simulate the registerReceiver overload with flags.
    @Config(sdk = 23)
    public void registeredReceiverStillUpdatesBatteryState() {
        BatteryPowerWatcher watcher = new BatteryPowerWatcher();
        watcher.register(RuntimeEnvironment.getApplication());
        try {
            Intent battery = new Intent(Intent.ACTION_BATTERY_CHANGED)
                    .putExtra(BatteryManager.EXTRA_LEVEL, 75)
                    .putExtra(BatteryManager.EXTRA_SCALE, 100)
                    .putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_CHARGING)
                    .putExtra(BatteryManager.EXTRA_PLUGGED, BatteryManager.BATTERY_PLUGGED_USB)
                    .putExtra(BatteryManager.EXTRA_PRESENT, true);
            RuntimeEnvironment.getApplication().sendBroadcast(battery);
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();

            assertEquals(75, watcher.batteryBean.getLevel());
            assertEquals(BatteryManager.BATTERY_PLUGGED_USB, watcher.batteryBean.getPlugState());
            assertEquals(BatteryManager.BATTERY_STATUS_CHARGING, watcher.batteryBean.getBatteryStatue());
            assertTrue(watcher.batteryBean.isBatteryPresent());
        } finally {
            watcher.unRegister(RuntimeEnvironment.getApplication());
        }
    }

    private static class RecordingContext extends ContextWrapper {
        BroadcastReceiver receiver;
        BroadcastReceiver unregisteredReceiver;
        IntentFilter filter;
        Integer flags;

        RecordingContext() {
            super(RuntimeEnvironment.getApplication());
        }

        @Override
        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
            this.receiver = receiver;
            this.filter = filter;
            return null;
        }

        @Override
        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter,
                                       String broadcastPermission, Handler scheduler, int flags) {
            assertNull(broadcastPermission);
            assertNull(scheduler);
            this.flags = flags;
            return registerReceiver(receiver, filter);
        }

        @Override
        public void unregisterReceiver(BroadcastReceiver receiver) {
            unregisteredReceiver = receiver;
        }
    }
}
