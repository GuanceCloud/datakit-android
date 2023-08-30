package com.ft.sdk.garble.utils;

/**
 * 根据编辑后 Runtime 类映射来判断，应用程序是否对一个库进行依赖
 *
 * @author Brandon
 */
public class PackageUtils {
    /**
     *  native SDK 依赖主类 package 路径
     */
    private static final String PACKAGE_NATIVE_ENGINE_CLASS = "com.ft.sdk.nativelib.NativeEngineInit";

    /**
     *  Okhttp 主类 package 路径
     */
    private static final String PACKAGE_OKHTTP3 = "okhttp3.OkHttpClient";

    /**
     * Flutter Application package 路径
     */
    private static final String PACKAGE_FLUTTER = "io.flutter.app.FlutterApplication";
    /**
     * React Native Application package 路径
     */
    private static final String PACKAGE_REACT_NATIVE = "com.facebook.react.ReactApplication";

    /**
     * alibaba taobao SophixApplication package 路径
     */
    private static final String PACKAGE_SOPHIX = "com.taobao.sophix.SophixApplication";

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
     * 获取 SophixApplication 的 class
     * @return
     */
    public static Class<?> getSophixClass() {
        try {
            return Class.forName(PACKAGE_SOPHIX);
        } catch (ClassNotFoundException ignored) {
        }
        return null;
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
