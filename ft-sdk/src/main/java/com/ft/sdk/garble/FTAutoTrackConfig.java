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

    //以下三个为白名单
    private int enableAutoTrackType;
    private List<Integer> onlyAutoTrackActivities;
    private List<Class<?>> onlyAutoTrackViews;

    //以下三个为设置黑名单
    private int disableAutoTrackType;
    private List<Integer> ignoreAutoTrackActivities;
    private List<Class<?>> ignoreAutoTrackViews;
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
        addOnlyAutoTrackActivity(ftsdkConfig.getWhiteActivityClass());
        addOnlyAutoTrackView(ftsdkConfig.getWhiteViewClass());

        disableAutoTrackType = ftsdkConfig.getDisableAutoTrackType();
        addIgnoreAutoTrackActivity(ftsdkConfig.getBlackActivityClass());
        addIgnoreAutoTrackView(ftsdkConfig.getBlackViewClass());
    }

    private void addOnlyAutoTrackActivity(List<Class<?>> classes) {
        if (classes != null && !classes.isEmpty()) {
            if (onlyAutoTrackActivities == null) {
                onlyAutoTrackActivities = new ArrayList<>();
            }
            int hashCode;
            for (Class<?> activity : classes) {
                hashCode = activity.hashCode();
                if (!onlyAutoTrackActivities.contains(hashCode)) {
                    onlyAutoTrackActivities.add(hashCode);
                }
            }
        }
    }

    private void addOnlyAutoTrackView(List<Class<?>> classes) {
        if (classes != null && !classes.isEmpty()) {
            if (onlyAutoTrackViews == null) {
                onlyAutoTrackViews = new ArrayList<>();
            }
            for (Class<?> view : classes) {
                if (!onlyAutoTrackViews.contains(view)) {
                    onlyAutoTrackViews.add(view);
                }
            }
        }
    }

    private void addIgnoreAutoTrackActivity(List<Class<?>> classes) {
        if (classes != null && !classes.isEmpty()) {
            if (ignoreAutoTrackActivities == null) {
                ignoreAutoTrackActivities = new ArrayList<>();
            }
            int hashCode;
            for (Class<?> activity : classes) {
                hashCode = activity.hashCode();
                if (!ignoreAutoTrackActivities.contains(hashCode)) {
                    ignoreAutoTrackActivities.add(hashCode);
                }
            }
        }
    }

    private void addIgnoreAutoTrackView(List<Class<?>> classes) {
        if (classes != null && !classes.isEmpty()) {
            if (ignoreAutoTrackViews == null) {
                ignoreAutoTrackViews = new ArrayList<>();
            }
            for (Class<?> view : classes) {
                if (!ignoreAutoTrackViews.contains(view)) {
                    ignoreAutoTrackViews.add(view);
                }
            }
        }
    }

    /**
     * 是否开启自动埋点
     *
     * @return
     */
    public boolean isAutoTrack() {
        return autoTrack;
    }

    /**
     * 自动埋点类型白名单
     *
     * @param type
     * @return
     */
    public boolean enableAutoTrackType(FTAutoTrackType type) {
        if(enableAutoTrackType == 0){
            return true;
        }
        if ((enableAutoTrackType | type.type) == enableAutoTrackType) {
            return true;
        }
        return false;
    }

    /**
     * Activity 是否是白名单
     *
     * @param activity
     * @return
     */
    public boolean isOnlyAutoTrackActivity(Class<?> activity) {
        if (activity == null) {
            return true;
        }
        if (onlyAutoTrackActivities == null || onlyAutoTrackActivities.isEmpty()) {
            return true;
        }
        if (onlyAutoTrackActivities.contains(activity.hashCode())) {
            return true;
        }
        return false;
    }

    /**
     * View 是否在白名单里
     *
     * @param view
     * @return
     */
    public boolean isOnlyView(View view) {
        try {
            if (view == null) {
                return true;
            }
            if (onlyAutoTrackViews == null || onlyAutoTrackViews.isEmpty()) {
                return true;
            }

            for (Class<?> clazz : onlyAutoTrackViews) {
                if (clazz.isAssignableFrom(view.getClass())) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * View 是否在白名单里
     *
     * @param viewClass
     * @return
     */
    public boolean isOnlyView(Class viewClass) {
        try {
            if (viewClass == null) {
                return true;
            }
            if (onlyAutoTrackViews == null || onlyAutoTrackViews.isEmpty()) {
                return true;
            }

            for (Class<?> clazz : onlyAutoTrackViews) {
                if (clazz.isAssignableFrom(viewClass.getClass())) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 自动埋点类型黑名单
     *
     * @param type
     * @return
     */
    public boolean disableAutoTrackType(FTAutoTrackType type) {
        if(disableAutoTrackType == 0){
            return false;
        }
        if ((disableAutoTrackType | type.type) == disableAutoTrackType) {
            return true;
        }
        return false;
    }

    /**
     * Activity 黑名单
     *
     * @param activity
     * @return
     */
    public boolean isIgnoreAutoTrackActivity(Class<?> activity) {
        if (activity == null) {
            return false;
        }
        if (ignoreAutoTrackActivities != null && ignoreAutoTrackActivities.contains(activity.hashCode())) {
            return true;
        }
        return false;
    }

    /**
     * View 黑名单
     *
     * @param view View
     * @return 是否被忽略
     */
    public boolean isIgnoreView(View view) {
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

    /**
     * View 黑名单
     *
     * @param viewClass View
     * @return 是否被忽略
     */
    public boolean isIgnoreView(Class viewClass) {
        try {
            if (viewClass == null) {
                return false;
            }
            if (ignoreAutoTrackViews != null) {
                for (Class<?> clazz : ignoreAutoTrackViews) {
                    if (clazz.isAssignableFrom(viewClass.getClass())) {
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
