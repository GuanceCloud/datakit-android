package com.ft.sdk;

/**
 * BY huangDianHua
 * DATE:2020-01-09 17:16
 * Description:
 */
public class MonitorType {
    public static int ALL = 0xFFFFFFFF;
    public static int BATTERY = 1<<1;
    public static int MEMORY = 1<<2;
    public static int CPU = 1<<3;
//    public static int GPU = 1<<4;·
//    public static int NETWORK = 1<<5;
//    public static int CAMERA = 1<<6;
//    public static int LOCATION = 1<<7;
    public static int SYSTEM = 1<<8;
//    public static int SENSOR = 1<<9;
    public static int BLUETOOTH = 1<<10;
//    public static int SENSOR_BRIGHTNESS = 1 << 11;
//    public static int SENSOR_STEP = 1 << 12;
//    public static int SENSOR_PROXIMITY = 1 << 13;
//    public static int SENSOR_ROTATION = 1 << 14;
//    public static int SENSOR_ACCELERATION = 1 << 15;
//    public static int SENSOR_MAGNETIC = 1 << 16;
//    public static int SENSOR_LIGHT = 1 << 17;
//    public static int SENSOR_TORCH = 1 << 18;
    public static int FPS = 1 << 19;

}
