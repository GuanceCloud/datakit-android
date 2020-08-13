
package com.ft.plugin.garble;

/**
 * author: huangDianHua
 * time: 2020/8/5 13:37:00
 * description:常量
 */
public class Constants {
    /**
     * SDK 中的插桩类
     */
    public static final String FT_SDK_API = "com/ft/sdk/FTAutoTrack";
    /**
     * {@link System}
     */
    public static final String CLASS_NAME_SYSTEM = "java/lang/System";
    /**
     * Android 系统日志类
     */
    public static final String CLASS_NAME_LOG = "android/util/Log";

    /**
     * SDK 中日志插桩类
     */
    public static final String CLASS_NAME_TRACKLOG = "com/ft/sdk/garble/utils/TrackLog";
    /**
     * int A(String,String) 方法的描述
     */
    public static final String METHOD_DESC_S_S_I = "(Ljava/lang/String;Ljava/lang/String;)I";
    /**
     * int v(String tag, String msg, Throwable tr) 方法描述
     */
    public static final String METHOD_DESC_S_S_T_I = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I";

    /**
     * int w(String tag, Throwable tr)
     */
    public static final String METHOD_DESC_S_T_I = "(Ljava/lang/String;Ljava/lang/Throwable;)I";
}