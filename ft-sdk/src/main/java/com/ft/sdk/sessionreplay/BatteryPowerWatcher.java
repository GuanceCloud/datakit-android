package com.ft.sdk.sessionreplay;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;

import com.ft.sdk.garble.bean.BatteryBean;

public class BatteryPowerWatcher extends BroadcastReceiver {

    //  API 26-32; native enforcement starts at API 33.
    private static final int RECEIVER_NOT_EXPORTED_COMPAT = 0x4;

    final BatteryBean batteryBean = new BatteryBean();

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        // Check if it is a battery status update broadcast
        if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
            int pluggedStatus = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean batteryPresent = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true);

            int batteryPct = (int) ((level / (float) scale) * 100);

            if (powerManager != null) {
                batteryBean.setLevel(batteryPct);
                batteryBean.setPlugState(pluggedStatus);
                batteryBean.setBatteryPresent(batteryPresent);
                batteryBean.setBatteryStatue(status);
            }
        }

        // Check if it is a power saving mode change broadcast
        if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(intent.getAction())) {
            if (powerManager != null) {
                batteryBean.setSaveMode(powerManager.isPowerSaveMode());
            }
        }
    }

    // Register this receiver to listen for power save mode and battery changes
    @SuppressLint({"WrongConstant", "UnspecifiedRegisterReceiverFlag"})
    public void register(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(this, filter, null, null, Context.RECEIVER_NOT_EXPORTED);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(this, filter, null, null, RECEIVER_NOT_EXPORTED_COMPAT);
        } else {
            context.registerReceiver(this, filter);
        }
    }

    public void unRegister(Context context) {
        context.unregisterReceiver(this);
    }


}
