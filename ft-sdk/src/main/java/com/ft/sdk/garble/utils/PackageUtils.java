package com.ft.sdk.garble.utils;

public class PackageUtils {

    private static final String PACKAGE_NATIVE_ENGINE_CLASS = "com.ft.sdk.nativelib.NativeEngineInit";
    private static final String PACKAGE_OKHTTP3 = "okhttp3.OkHttpClient";

    public static boolean isNativeLibrarySupport() {
        try {
            Class.forName(PACKAGE_NATIVE_ENGINE_CLASS);
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    public static boolean isOKHttp3Support() {
        try {
            Class.forName(PACKAGE_OKHTTP3);
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;

    }

}
