package com.ft.sdk.garble.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.ft.sdk.FTApplication;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    public boolean isOpen(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }

    public Set<BluetoothDevice> getBondedDevices(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.getBondedDevices();
    }

    public void getBluetooth(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        //设置过滤器，过滤因远程蓝牙设备被找到而发送的广播 BluetoothDevice.ACTION_FOUND
        IntentFilter iFilter=new IntentFilter();
        iFilter.addAction(BluetoothDevice.ACTION_FOUND);
        //设置广播接收器和安装过滤器
        FTApplication.getApplication().registerReceiver(new foundReceiver(), iFilter);
        bluetoothAdapter.startDiscovery();
    }

    class foundReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            try {
                if (intent != null) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//获取此时找到的远程设备对象
                    if (device != null && !Utils.isNullOrEmpty(device.getName())) {
                        short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);//获取额外rssi值
                        LogUtils.d("BluetoothDevice-info:\n" +
                                "name:" + device.getName() + "\n" +
                                "rssi:" + rssi+"\n" +
                                "address:"+device.getAddress());
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

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
