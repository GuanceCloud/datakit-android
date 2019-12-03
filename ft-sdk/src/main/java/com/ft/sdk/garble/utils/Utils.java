package com.ft.sdk.garble.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.core.content.ContextCompat;

import com.ft.sdk.FTApplication;

/**
 * BY huangDianHua
 * DATE:2019-11-29 17:54
 * Description:
 */
public class Utils {
    public static boolean hasNetPermission() {
        return hasPermission(FTApplication.getApplication(), Manifest.permission.ACCESS_NETWORK_STATE) &&
                hasPermission(FTApplication.getApplication(), Manifest.permission.INTERNET);
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED;
    }

    public boolean isNetworkAvailable() {
        return isNetworkAvailable(FTApplication.getApplication());
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetWork = manager.getActiveNetworkInfo();
        return activeNetWork != null && activeNetWork.isConnected();
    }
}

