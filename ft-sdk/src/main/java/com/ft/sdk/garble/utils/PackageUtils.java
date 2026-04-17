package com.ft.sdk.garble.utils;

import java.lang.reflect.Field;

/**
 * Determine whether an application depends on a library based on the edited Runtime class mapping
 *
 * @author Brandon
 */
public class PackageUtils {
    /**
     * native SDK dependency main class package path
     */
    private static final String PACKAGE_NATIVE_ENGINE_CLASS = "com.ft.sdk.nativelib.NativeEngineInit";

    /**
     * Okhttp main class package path
     */
    private static final String PACKAGE_OKHTTP3 = "okhttp3.OkHttpClient";

    /**
     * Flutter Application package path
     */
    private static final String PACKAGE_FLUTTER = "io.flutter.app.FlutterApplication";
    /**
     * React Native Application package path
     */
    private static final String PACKAGE_REACT_NATIVE = "com.facebook.react.ReactApplication";

    /**
     * alibaba taobao SophixApplication package path
     */
    private static final String PACKAGE_SOPHIX = "com.taobao.sophix.SophixApplication";
    /**
     * nativelib BuildConfig package path
     */
    public static final String PACKAGE_NATIVELIB = "com.ft.sdk.nativelib.BuildConfig";
    /**
     * session replay BuildConfig package path
     */
    public static final String PACKAGE_SESSION_REPLAY = "com.ft.sdk.sessionreplay.BuildConfig";
    /**
     * session replay material BuildConfig package path
     */
    public static final String PACKAGE_SESSION_REPLAY_MTR = "com.ft.sdk.sessionreplay.material.BuildConfig";

    /**
     * Get version field
     */
    public static final String PACKAGE_FIELD_VERSION_NAME = "VERSION_NAME";

    /**
     * TBS WebView package path
     */
    private static final String PACKAGE_TBS_WEBVIEW = "com.tencent.smtt.sdk.WebView";


    /**
     * DCWebView
     */
    private static final String PACKAGE_DCLOUD_WEBVIEW = "io.dcloud.common.adapter.ui.webview.DCWebView";

    /**
     * Whether to use NDK library
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
     * Get native library version👌
     *
     * @return
     */
    public static String getNativeLibVersion() {
        return getPackVersion(PACKAGE_NATIVELIB);
    }

    /**
     * Get session replay library version
     *
     * @return
     */
    public static String getPackageSessionReplay() {
        return getPackVersion(PACKAGE_SESSION_REPLAY);
    }

    /**
     * Get session replay material library version
     *
     * @return
     */
    public static String getPackageSessionReplayMtr() {
        return getPackVersion(PACKAGE_SESSION_REPLAY_MTR);
    }

    /**
     * Whether to depend on okhttp3
     *
     * @return
     */
    public static boolean isOKHttp3Support() {
        return isPackageExist(PACKAGE_OKHTTP3);
    }

    /**
     * Get SophixApplication class
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
     * Determine if it's third-party framework data integration
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

    /**
     * {"agent":"x.x.x", "native":"x.x.x", "track":"x.x.x"} Add in this fixed format
     *
     * @param json
     * @param key
     * @param value
     * @return
     */
    public static String appendPackageVersion(String json, String key, String value) {
        if (json.startsWith("{")) {
            // Insert new key-value pair after the first `{`
            return json.substring(0, 1) +
                    "\"" + key + "\":\"" + value + "\", " +
                    json.substring(1);
        }

        // If not JSON format, return original string directly
        return json;

    }


    public static boolean isAndroidXAvailable() {
        try {
            Class.forName("androidx.fragment.app.FragmentActivity");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Whether TBS WebView is available
     *
     * @return true if TBS WebView is available
     */
    public static boolean isTBSWebViewAvailable() {
        try {
            Class.forName(PACKAGE_TBS_WEBVIEW);
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    /**
     * Whether DC WebView is available
     *
     * @return true if DC WebView is available
     */
    public static boolean isDCSWebViewAvailable() {
        try {
            Class.forName(PACKAGE_DCLOUD_WEBVIEW);
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

}
