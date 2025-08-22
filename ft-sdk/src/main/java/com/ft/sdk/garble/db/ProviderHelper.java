package com.ft.sdk.garble.db;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.ft.sdk.garble.utils.Utils;

public class ProviderHelper {
    private static volatile String authority;

    public static String getAuthority(Context context) {
        if (authority == null) {
            synchronized (ProviderHelper.class) {
                if (authority == null) {
                    authority = loadAuthority(context);
                }
            }
        }
        return authority;
    }

    private static String loadAuthority(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                String custom = appInfo.metaData.getString("com.ft.sdk.PROVIDER_AUTHORITY");
                if (!Utils.isNullOrEmpty(custom)) {
                    return custom;
                }
            }
        } catch (Exception ignored) {
        }
        return context.getPackageName() + "." + FTContentProvider.PACKAGE_AUTHORITY_SUBFIX;
    }
}