package com.ft.sdk;

/**
 * BY huangDianHua
 * DATE:2019-12-20 14:36
 * Description:
 */
public enum FTAutoTrackType {
    APP_SATRT(1),APP_END(1<<1),APP_CLICK(1<<2);
    public int type;
    FTAutoTrackType(int type){
        this.type = type;
    }
}
