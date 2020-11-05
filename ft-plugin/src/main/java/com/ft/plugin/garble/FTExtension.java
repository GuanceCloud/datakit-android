package com.ft.plugin.garble;

/**
 * BY huangDianHua
 * DATE:2019-11-29 14:50
 * Description:
 */
public class FTExtension {
    public boolean showLog = false;//是否显示日志
    public boolean openAutoTrack = true;//是否打开自动track
    public boolean autoUploadProguardMap = false; //是否自动上 map 文件

    @Override
    public String toString() {
        return "FTExtension{" +
                " showLog=" + showLog +
                ", openAutoTrack=" + openAutoTrack +
                ", autoUploadProguardMap=" + autoUploadProguardMap +
                '}';
    }
}
