package com.ft.sdk.garble.utils;

import java.lang.reflect.Field;

/**
 * 根据编辑后 Runtime 类映射来判断，应用程序是否对一个库进行依赖
 *
 * @author Brandon
 */
public class PackageUtils {
    /**
     * native SDK 依赖主类 package 路径
     */
    private static final String PACKAGE_NATIVE_ENGINE_CLASS = "com.ft.sdk.nativelib.NativeEngineInit";

    /**
     * Okhttp 主类 package 路径
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
     * nativelib BuildConfig package 路径
     */
    public static final String PACKAGE_NATIVELIB = "com.ft.sdk.nativelib.BuildConfig";
    /**
     * session replay BuildConfig package 路径
     */
    public static final String PACKAGE_SESSION_REPLAY = "com.ft.sdk.sessionreplay.BuildConfig";
    /**
     * session replay material BuildConfig package 路径
     */
    public static final String PACKAGE_SESSION_REPLAY_MTR = "com.ft.sdk.sessionreplay.material.BuildConfig";

    /**
     * 获取版本 field
     */
    public static final String PACKAGE_FIELD_VERSION_NAME = "VERSION_NAME";

    /**
     * 是否使用 NDK 库
     *
     * @return
     */
    public static boolean isNativeLibrarySupport() {
        return isPackageExist(PACKAGE_NATIVE_ENGINE_CLASS);
    }

    public static boolean isSessionReplay() {
        return isPackageExist(PACKAGE_SESSION_REPLAY);
    }

    public static boolean isSessionReplayMtr() {
        return isPackageExist(PACKAGE_SESSION_REPLAY_MTR);
    }


    /**
     * 获取 native library 库的版本
     *
     * @return
     */
    public static String getNativeLibVersion() {
        return getPackVersion(PACKAGE_NATIVELIB);
    }

    /**
     * 获取 session replay 库版本
     *
     * @return
     */
    public static String getPackageSessionReplay() {
        return getPackVersion(PACKAGE_SESSION_REPLAY);
    }

    /**
     * 获取 session replay material 库版本
     *
     * @return
     */
    public static String getPackageSessionReplayMtr() {
        return getPackVersion(PACKAGE_SESSION_REPLAY_MTR);
    }

    /**
     * 是否依赖 okhttp3
     *
     * @return
     */
    public static boolean isOKHttp3Support() {
        return isPackageExist(PACKAGE_OKHTTP3);
    }

    /**
     * 获取 SophixApplication 的 class
     *
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
     *
     * @return
     */
    public static boolean isThirdPartySupport() {
        if (isPackageExist(PACKAGE_FLUTTER)) {
            return true;
        }
        return isPackageExist(PACKAGE_REACT_NATIVE);
    }

    /**
     * @param pkgName
     * @return
     */
    private static boolean isPackageExist(String pkgName) {
        try {
            Class.forName(pkgName);
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    /**
     * @param pkgVersionPath
     * @return
     */
    private static String getPackVersion(String pkgVersionPath) {
        try {
            Class<?> buildConfigClass = Class.forName(pkgVersionPath);
            Field versionNameField = buildConfigClass.getField(PACKAGE_FIELD_VERSION_NAME);
            return (String) versionNameField.get(null);
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return "";
    }
}
