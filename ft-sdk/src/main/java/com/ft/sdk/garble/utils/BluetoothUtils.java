package com.ft.sdk.garble.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Build;

import com.ft.sdk.FTApplication;

import java.lang.reflect.Field;

/**
 * create: by huangDianHua
 * time: 2020/4/16 10:04:04
 * description:蓝牙相关信息获取类
 */
public class BluetoothUtils {
    private static BluetoothUtils instance;
    private Context mContext = FTApplication.getApplication();
    String bluetoothMacAddress;

    private BluetoothUtils() {
    }

    public static BluetoothUtils get() {
        if (instance == null) {
            instance = new BluetoothUtils();
        }
        return instance;
    }

    public String getDeviceName() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.getName();
    }

    /**
     * 获取设备的蓝牙 MAC 地址
     *
     * @return
     */
    public String getBluetoothMacAddress() {
        if (!Utils.isNullOrEmpty(bluetoothMacAddress)) {
            return bluetoothMacAddress;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                Field mServiceField = bluetoothAdapter.getClass().getDeclaredField("mService");
                mServiceField.setAccessible(true);
                Object btManagerService = mServiceField.get(bluetoothAdapter);
                if (btManagerService != null) {
                    bluetoothMacAddress = (String) btManagerService.getClass().getMethod("getAddress").invoke(btManagerService);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothMacAddress = android.provider.Settings.Secure.getString(mContext.getContentResolver(), "bluetooth_address");
        } else {
            bluetoothMacAddress = bluetoothAdapter.getAddress();
        }
        return bluetoothMacAddress;
    }

    public void release() {
        instance = null;
    }
}
