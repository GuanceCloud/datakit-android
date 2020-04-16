package com.ft.sdk.garble.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.ft.sdk.FTApplication;

/**
 * create: by huangDianHua
 * time: 2020/4/16 10:04:04
 * description:蓝牙相关信息获取类
 */
public class BluetoothUtils {
    private static BluetoothUtils instance;
    private Context mContext = FTApplication.getApplication();
    private BluetoothUtils(){
        initBluetooth();
    }
    public static BluetoothUtils get(){
        if(instance == null){
            instance = new BluetoothUtils();
        }
        return instance;
    }

    private void initBluetooth(){
        BluetoothManager manager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if(manager != null) {
            BluetoothAdapter adapter = manager.getAdapter();
            LogUtils.d("Bluetooth-> name:"+adapter.getName()+"address:"+adapter.getAddress());
        }
    }

    public void release(){
        instance = null;
    }
}
