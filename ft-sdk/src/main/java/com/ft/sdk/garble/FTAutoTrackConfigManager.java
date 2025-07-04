package com.ft.sdk.garble;

import android.view.View;

import com.ft.sdk.FTAutoTrackType;

import java.util.ArrayList;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-12-19 18:59
 * Description:
 */
public class FTAutoTrackConfigManager {
    private boolean autoTrack = true;

    //The following three are whitelists
    private int enableAutoTrackType;
    private List<Integer> onlyAutoTrackActivities;
    private List<Class<?>> onlyAutoTrackViews;

    //The following three are blacklist settings
    private int disableAutoTrackType;
    private List<Integer> ignoreAutoTrackActivities;
    private List<Class<?>> ignoreAutoTrackViews;
    private static FTAutoTrackConfigManager instance;


    private FTAutoTrackConfigManager() {
    }

    public synchronized static FTAutoTrackConfigManager get() {
        if (instance == null) {
            instance = new FTAutoTrackConfigManager();
        }
        return instance;
    }

    public void initParams() {
//        enableAutoTrackType = ftsdkConfig.getEnableAutoTrackType();
//        addOnlyAutoTrackActivity(ftsdkConfig.getWhiteActivityClass());
//        addOnlyAutoTrackView(ftsdkConfig.getWhiteViewClass());
//
//        disableAutoTrackType = ftsdkConfig.getDisableAutoTrackType();
//        addIgnoreAutoTrackActivity(ftsdkConfig.getBlackActivityClass());
//        addIgnoreAutoTrackView(ftsdkConfig.getBlackViewClass());
    }

    /**
     * Add Activity whitelist
     *
     * @param classes
     */
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

    /**
     * Add control whitelist
     *
     * @param classes
     */
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

    /**
     * Add Activity blacklist
     *
     * @param classes
     */
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

    /**
     * Add control blacklist
     *
     * @param classes
     */
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
     * Whether to enable automatic tracking
     *
     * @return
     */
    public boolean isAutoTrack() {
        return autoTrack;
    }

    /**
     * Automatic tracking type whitelist
     *
     * @param type
     * @return
     */
    public boolean enableAutoTrackType(FTAutoTrackType type) {
        if (enableAutoTrackType == 0) {
            return true;
        }
        return (enableAutoTrackType | type.type) == enableAutoTrackType;
    }

    /**
     * Whether Activity is in whitelist
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
        return onlyAutoTrackActivities.contains(activity.hashCode());
    }

    /**
     * Whether View is in whitelist
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
     * Whether View is in whitelist
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
     * Automatic tracking type blacklist
     *
     * @param type
     * @return
     */
    public boolean disableAutoTrackType(FTAutoTrackType type) {
        if (disableAutoTrackType == 0) {
            return false;
        }
        return (disableAutoTrackType | type.type) == disableAutoTrackType;
    }

    /**
     * Activity blacklist
     *
     * @param activity
     * @return
     */
    public boolean isIgnoreAutoTrackActivity(Class<?> activity) {
        if (activity == null) {
            return false;
        }
        return ignoreAutoTrackActivities != null && ignoreAutoTrackActivities.contains(activity.hashCode());
    }

    /**
     * View blacklist
     *
     * @param view View
     * @return Whether it is ignored
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
     * View blacklist
     *
     * @param viewClass View
     * @return Whether it is ignored
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

    public static void release() {
        instance = null;
    }
}
