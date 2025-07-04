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
     * Whether to use NDK library
     *
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
     * Get native library version👌
     *
     * @return
     */
    public static String getNativeLibVersion() {

        try {
            Class<?> buildConfigClass = Class.forName("com.ft.sdk.nativelib.BuildConfig");
            Field versionNameField = buildConfigClass.getField("VERSION_NAME");
            return (String) versionNameField.get(null);
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
     * Whether to depend on okhttp3
     *
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

}
