package com.ft.sdk;

/**
 * BY huangDianHua
 * DATE:2019-12-20 14:36
 *
 * 声明自动捕获类型，用于建立过滤体系，用于之后 {@link FTAutoTrack} 黑白名单配置
 *
 */
public enum FTAutoTrackType {
//    APP_START(1),
//    APP_END(1 << 1),
    /**
     * 应用点击事件
     */
    APP_CLICK(1 << 2);

    /**
     *
     */
    public final int type;

    /**
     * 内部构造方法
     * @param type
     */
    FTAutoTrackType(int type) {
        this.type = type;
    }
}
