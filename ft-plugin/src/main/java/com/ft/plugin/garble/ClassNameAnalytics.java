package com.ft.plugin.garble;

/**
 * BY huangDianHua
 * DATE:2019-12-03 13:14
 * Description:
 */
public class ClassNameAnalytics {
    public static final String FT_SDK_PACKAGE = "com.ft.sdk";

    public static boolean isFTSDKFile(String className){
        return className.startsWith(FT_SDK_PACKAGE);
    }
    
    public static boolean isAndroidGenerated(String className){
        return className.contains("R$") ||
                className.contains("R2$") ||
                className.contains("R.class") ||
                className.contains("R2.class") ||
                className.contains("BuildConfig.class");
    }
}
