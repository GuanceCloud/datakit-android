package com.ft.sdk.garble.utils;

public class PackageUtils {

    private static final String PACKAGE_NATIVE_ENGINE_CLASS = "com.ft.sdk.nativelib.NativeEngineInit";
    private static final String PACKAGE_OKHTTP3 = "okhttp3.OkHttpClient";
    private static final String PACKAGE_FLUTTER = "io.flutter.app.FlutterApplication";
    private static final String PACKAGE_REACT_NATIVE = "com.facebook.react.ReactApplication";

    /**
     * 是否使用 NDK 库
     * @return
     */
    public static boolean isNativeLibrarySupport() {
        try {
            Class.forName(PACKAGE_NATIVE_ENGINE_CLASS);
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    /**
     * 是否依赖 okhttp3
     * @return
     */
    public static boolean isOKHttp3Support() {
        try {
            Class.forName(PACKAGE_OKHTTP3);
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;

    }

    /**
     * 判断是否是第三方框架数据接入
     * @return
     */
    public static boolean isThirdPartySupport() {
        try {
            Class.forName(PACKAGE_FLUTTER);
            return true;
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class.forName(PACKAGE_REACT_NATIVE);
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

}
