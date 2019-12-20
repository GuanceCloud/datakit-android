package com.ft.sdk.garble;

import android.view.View;

import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTSDKConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-12-19 18:59
 * Description:
 */
public class FTAutoTrackConfig {
    private boolean autoTrack;
    private int enableAutoTrackType;
    private ArrayList<Integer> ignoreAutoTrackActivitys;
    private ArrayList<Class<?>> ignoreAutoTrackViews;
    private static FTAutoTrackConfig instance;


    private FTAutoTrackConfig() {
    }

    public synchronized static FTAutoTrackConfig get() {
        if (instance == null) {
            instance = new FTAutoTrackConfig();
        }
        return instance;
    }

    public void initParams(FTSDKConfig ftsdkConfig) {
        if (ftsdkConfig == null) {
            return;
        }
        autoTrack = ftsdkConfig.isAutoTrack();
        enableAutoTrackType = ftsdkConfig.getEnableAutoTrackType();
        addIgnoreAutoTrackActivity(ftsdkConfig.getIgnoreAutoTrackActivitys());
        addIgnoreAutoTrackView(ftsdkConfig.getIgnoreAutoTrackViews());
    }

    private void addIgnoreAutoTrackActivity(List<Class<?>> classes){
        if(classes != null && !classes.isEmpty()){
            if(ignoreAutoTrackActivitys == null){
                ignoreAutoTrackActivitys = new ArrayList<>();
            }
            int hashCode;
            for (Class<?> activity:classes){
                hashCode = activity.hashCode();
                if(!ignoreAutoTrackActivitys.contains(hashCode)){
                    ignoreAutoTrackActivitys.add(hashCode);
                }
            }
        }
    }

    private void addIgnoreAutoTrackView(List<Class<?>> classes){
        if(classes != null && !classes.isEmpty()){
            if(ignoreAutoTrackViews == null){
                ignoreAutoTrackViews = new ArrayList<>();
            }
            for (Class<?> view:classes){
                if(!ignoreAutoTrackViews.contains(view)){
                    ignoreAutoTrackViews.add(view);
                }
            }
        }
    }
    /**
     * 是否忽略Activity
     * @param activity
     * @return
     */
    public boolean isIgnoreAutoTrackActivity(Class<?> activity){
        if(activity == null){
            return false;
        }
        if(ignoreAutoTrackActivitys != null && ignoreAutoTrackActivitys.contains(activity.hashCode())){
            return true;
        }
        return false;
    }

    /**
     * 是否开启自动埋点
     * @return
     */
    public boolean isAutoTrack() {
        return autoTrack;
    }

    /**
     * 开启的自动埋点类型
     * @param type
     * @return
     */
    public boolean enableAutoTrackType(FTAutoTrackType type){
        if((enableAutoTrackType|type.type) == enableAutoTrackType){
            return true;
        }
        return false;
    }

    /**
     * 判断 View 是否被忽略
     *
     * @param view View
     * @return 是否被忽略
     */
    public boolean isViewIgnored(View view) {
        try {
            if (view == null) {
                return false;
            }
            if (ignoreAutoTrackViews != null) {
                for (Class<?> clazz : ignoreAutoTrackViews) {
                    if (clazz.isAssignableFrom(view.getClass())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
