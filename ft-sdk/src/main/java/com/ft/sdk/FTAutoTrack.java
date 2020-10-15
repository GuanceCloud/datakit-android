package com.ft.sdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.garble.FTAliasConfig;
import com.ft.sdk.garble.FTAutoTrackConfig;
import com.ft.sdk.garble.FTFlowConfig;
import com.ft.sdk.garble.FTFragmentManager;
import com.ft.sdk.garble.FTTrackInner;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.ObjectBean;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.bean.TrackBean;
import com.ft.sdk.garble.manager.FTActivityManager;
import com.ft.sdk.garble.manager.FTManager;
import com.ft.sdk.garble.manager.SyncDataHelper;
import com.ft.sdk.garble.utils.AopUtils;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
import com.ft.sdk.garble.utils.Utils;
import com.google.android.material.tabs.TabLayout;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Map;

import okhttp3.OkHttpClient;

/**
 * BY huangDianHua
 * DATE:2019-12-02 16:43
 * Description
 */
public class FTAutoTrack {
    public final static String TAG = "FTAutoTrack";

    /**
     * 启动 APP
     * 警告！！！该方法不能删除
     *
     * @deprecated 该方法原来被 FT Plugin 插件调用，目前不再使用。目前监控应用的启动使用{@link #startApp()}方法
     */
    @Deprecated
    public static void startApp(Object object) {
        try {
            startApp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Activity 打开的方式（标记是从 Fragment 打开还是 Activity）
     * 警告！！！该方法不能删除
     * 该方法原来被 FT Plugin 插件调用
     *
     * @param fromFragment
     * @param intent
     */
    public static void startActivityByWay(Boolean fromFragment, Intent intent) {
        try {
            LogUtils.d(TAG, "activityFromWay=" + fromFragment + ",,,intent=" + intent);
            if (intent != null && intent.getComponent() != null) {
                FTActivityManager.get().putActivityOpenFromFragment(intent.getComponent().getClassName(), fromFragment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Activity 开启
     * 警告！！！该方法不能删除
     *
     * @deprecated 该方法原来被 FT Plugin 插件调用，目前不再使用。目前监控应用的启动使用{@link #startPage(Class, boolean)}方法
     */
    @Deprecated
    public static void activityOnCreate(Class clazz) {
        try {
            //startPage(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Activity 关闭
     * 警告！！！该方法不能删除
     *
     * @deprecated 该方法原来被 FT Plugin 插件调用，目前不再使用。目前监控应用的启动使用{@link #destroyPage(Class)}方法
     */
    @Deprecated
    public static void activityOnDestroy(Class clazz) {
        try {
            //destroyPage(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知 Fragment 的显示隐藏状态
     * 警告！！！该方法不能删除
     *
     * @param clazz
     * @param activity
     * @param isVisible
     */
    public static void notifyUserVisibleHint(Object clazz, Object activity, boolean isVisible) {
        //LogUtils.d("Fragment[\n"+isVisible+"=====>fragment:"+((Class)clazz).getSimpleName());
        try {
            String className;
            if (activity == null) {
                Activity activity1 = FTActivityManager.get().getTopActivity();
                className = activity1.getClass().getName();
            } else {
                className = activity.getClass().getName();
            }
            FTFragmentManager.getInstance().setFragmentVisible(className, (Class) clazz, isVisible);
        } catch (Exception e) {
        }
    }

    /**
     * Fragment 打开
     * 警告！！！该方法不能删除
     *
     * @param clazz
     * @param activity
     * @deprecated 该方法原来被 FT Plugin 插件调用，目前不再使用。目前监控应用的启动使用{@link #startPage(Object, Object, String)}方法
     */
    @Deprecated
    public static void fragmentOnResume(Object clazz, Object activity) {
        try {
            //LogUtils.d("Fragment[\nOnCreateView=====>fragment:"+((Class)clazz).getSimpleName());
            //startPage(clazz, activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fragment 关闭
     * 警告！！！该方法不能删除
     *
     * @param clazz
     * @param activity
     * @deprecated 该方法原来被 FT Plugin 插件调用，目前不再使用。目前监控应用的启动使用{@link #destroyPage(Object, Object, String)}方法
     */
    @Deprecated
    public static void fragmentOnPause(Object clazz, Object activity) {
        try {
            //LogUtils.d("Fragment[\nOnDestroyView=====>fragment:"+((Class)clazz).getSimpleName());
            //destroyPage(clazz, activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 点击事件
     *
     * @param view
     */
    public static void trackViewOnClick(View view) {
        if (view == null) {
            return;
        }
        trackViewOnClick(null, view, view.isPressed());
    }

    /**
     * RadioGroup的点击选择事件
     *
     * @param group
     * @param checkedId
     */
    public static void trackRadioGroup(RadioGroup group, int checkedId) {
        if (group == null) {
            return;
        }

        trackViewOnClick(null, group, true);
    }

    /**
     * listView点击事件
     *
     * @param parent
     * @param v
     * @param position
     */
    public static void trackListView(AdapterView<?> parent, View v, int position) {
        trackViewOnClick(null, v, true);
    }

    /**
     * ExpandableList 父点击事件
     *
     * @param parent
     * @param v
     * @param position
     */
    public static void trackExpandableListViewOnGroupClick(ExpandableListView parent, View v, int position) {
        trackViewOnClick(null, v, true);
    }

    /**
     * TabHost切换
     *
     * @param tabName
     */
    public static void trackTabHost(String tabName) {
        //trackViewOnClick(null, v, true);
    }

    /**
     * TabLayout
     *
     * @param tab
     */
    public static void trackTabLayoutSelected(TabLayout.Tab tab) {
        try {
            Object object = tab.view.getContext();
            clickView(tab.view, object.getClass(), AopUtils.getClassName(object), AopUtils.getSupperClassName(object), AopUtils.getParentViewTree(tab.view) + "#" + tab.getPosition());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ViewPager 的页面切换
     *
     * @param object
     * @param position
     */
    public static void trackViewPagerChange(Object object, int position) {

    }

    /**
     * ExpandableList 子点击事件
     *
     * @param parent
     * @param v
     * @param parentPosition
     * @param childPosition
     */
    public static void trackExpandableListViewOnChildClick(ExpandableListView parent, View v, int parentPosition, int childPosition) {
        trackViewOnClick(null, v, true);
    }

    /**
     * 点击事件
     *
     * @param object
     * @param view
     */
    public static void trackViewOnClick(Object object, View view) {
        try {
            if (view == null) {
                return;
            }

            trackViewOnClick(object, view, view.isPressed());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 点击事件
     *
     * @param object
     * @param view
     * @param isFromUser
     */
    public static void trackViewOnClick(Object object, View view, boolean isFromUser) {
        try {
            if (isFromUser) {
                if (object == null) {
                    object = AopUtils.getActivityFromContext(view.getContext());
                }
                clickView(view, object.getClass(), AopUtils.getClassName(object), AopUtils.getSupperClassName(object), AopUtils.getViewTree(view));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackMenuItem(MenuItem menuItem) {
        try {
            trackMenuItem(null, menuItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackMenuItem(Object object, MenuItem menuItem) {
        try {
            clickView((Class<?>) object, AopUtils.getClassName(object), AopUtils.getSupperClassName(object), menuItem.getClass().getName() + "/" + menuItem.getItemId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackDialog(DialogInterface dialogInterface, int whichButton) {
        try {
            Dialog dialog = null;
            if (dialogInterface instanceof Dialog) {
                dialog = (Dialog) dialogInterface;
            }

            if (dialog == null) {
                return;
            }
            Context context = dialog.getContext();
            Activity activity = AopUtils.getActivityFromContext(context);
            if (activity == null) {
                activity = dialog.getOwnerActivity();
            }

            clickView(activity.getClass(), AopUtils.getClassName(activity), AopUtils.getSupperClassName(activity), AopUtils.getDialogClickView(dialog, whichButton));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static long startTimeline;

    /**
     * APP 启动
     */
    public static void startApp() {
        startTimeline = System.currentTimeMillis();
        if (!FTAutoTrackConfig.get().isAutoTrack()) {
            return;
        }
        if (!FTAutoTrackConfig.get().enableAutoTrackType(FTAutoTrackType.APP_START)) {
            return;
        }
        putPageEvent(System.currentTimeMillis(), OP.LANC, null, null, null, null);
    }


    /**
     * 应用休眠
     *
     * @param timeDelay
     */
    public static void sleepApp(long timeDelay) {
        long now = System.currentTimeMillis() - timeDelay;
        putClientTimeCost(now, OP.CLIENT_ACTIVATED_TIME, now - startTimeline);

    }

    /**
     * 页面卡顿
     */
    public static void uiBlock() {
        putSimpleEvent(OP.BLOCK);
    }

    /**
     * 应用崩溃
     */
    public static void appCrash() {
        putSimpleEvent(OP.CRASH);
    }

    /**
     * 应用无响应
     */
    public static void appAnr() {
        putSimpleEvent(OP.ANR);
    }


    /**
     * 打开某个Fragment页面
     *
     * @param clazz
     * @param activity
     */
    public static void startPage(Object clazz, Object activity, String parentPage) {
        /*没有开启自动埋点*/
        if (!FTAutoTrackConfig.get().isAutoTrack()) {
            return;
        }
        /*设置了白名单，但当前事件不在其中*/
        if (!FTAutoTrackConfig.get().enableAutoTrackType(FTAutoTrackType.APP_START)) {
            return;
        }

        /*设置了白名单，但当前页面不在其中*/
        if (!FTAutoTrackConfig.get().isOnlyAutoTrackActivity(activity.getClass())) {
            return;
        }
        /*设置了白名单，但当前页面不在其中*/
        if (!FTAutoTrackConfig.get().isOnlyAutoTrackActivity((Class<?>) clazz)) {
            return;
        }
        /*设置了黑名单，且事件在其中*/
        if (FTAutoTrackConfig.get().disableAutoTrackType(FTAutoTrackType.APP_START)) {
            return;
        }
        /*设置了黑名单，且页面在其中*/
        if (FTAutoTrackConfig.get().isIgnoreAutoTrackActivity(activity.getClass())) {
            return;
        }
        /*设置了黑名单，且页面在其中*/
        if (FTAutoTrackConfig.get().isIgnoreAutoTrackActivity((Class<?>) clazz)) {
            return;
        }
        putFragmentEvent(OP.OPEN_FRA, AopUtils.getClassName(clazz), AopUtils.getActivityName(activity), parentPage);
    }

    /**
     * 关闭某个Fragment
     *
     * @param clazz
     * @param activity
     */
    public static void destroyPage(Object clazz, Object activity, String parentPage) {
        /*没有开启自动埋点*/
        if (!FTAutoTrackConfig.get().isAutoTrack()) {
            return;
        }
        /*设置了白名单，但当前事件不在其中*/
        if (!FTAutoTrackConfig.get().enableAutoTrackType(FTAutoTrackType.APP_END)) {
            return;
        }

        /*设置了白名单，但当前页面不在其中*/
        if (!FTAutoTrackConfig.get().isOnlyAutoTrackActivity(activity.getClass())) {
            return;
        }
        /*设置了白名单，但当前页面不在其中*/
        if (!FTAutoTrackConfig.get().isOnlyAutoTrackActivity((Class<?>) clazz)) {
            return;
        }
        /*设置了黑名单，且事件在其中*/
        if (FTAutoTrackConfig.get().disableAutoTrackType(FTAutoTrackType.APP_END)) {
            return;
        }
        /*设置了黑名单，且页面在其中*/
        if (FTAutoTrackConfig.get().isIgnoreAutoTrackActivity(activity.getClass())) {
            return;
        }
        /*设置了黑名单，且页面在其中*/
        if (FTAutoTrackConfig.get().isIgnoreAutoTrackActivity((Class<?>) clazz)) {
            return;
        }
        putFragmentEvent(OP.CLS_FRA, AopUtils.getClassName(clazz), AopUtils.getActivityName(activity), parentPage);
    }

    /**
     * 打开某个Activity页面
     *
     * @param clazz
     */
    public static void startPage(Class<?> clazz, boolean isFirstLoad) {
        /*没有开启自动埋点*/
        if (!FTAutoTrackConfig.get().isAutoTrack()) {
            return;
        }
        /*设置了白名单，但当前事件不在其中*/
        if (!FTAutoTrackConfig.get().enableAutoTrackType(FTAutoTrackType.APP_START)) {
            return;
        }
        /*设置了白名单，但当前页面不在其中*/
        if (!FTAutoTrackConfig.get().isOnlyAutoTrackActivity(clazz)) {
            return;
        }
        /*设置了黑名单，且事件在其中*/
        if (FTAutoTrackConfig.get().disableAutoTrackType(FTAutoTrackType.APP_START)) {
            return;
        }
        /*设置了黑名单，且页面在其中*/
        if (FTAutoTrackConfig.get().isIgnoreAutoTrackActivity(clazz)) {
            return;
        }
        putActivityEvent(OP.OPEN_ACT, clazz, isFirstLoad);
    }

    /**
     * 关闭某个Activity
     *
     * @param clazz
     */
    public static void destroyPage(Class<?> clazz) {
        /*没有开启自动埋点*/
        if (!FTAutoTrackConfig.get().isAutoTrack()) {
            return;
        }
        /*设置了白名单，但当前事件不在其中*/
        if (!FTAutoTrackConfig.get().enableAutoTrackType(FTAutoTrackType.APP_END)) {
            return;
        }
        /*设置了白名单，但当前页面不在其中*/
        if (!FTAutoTrackConfig.get().isOnlyAutoTrackActivity(clazz)) {
            return;
        }
        /*设置了黑名单，且事件在其中*/
        if (FTAutoTrackConfig.get().disableAutoTrackType(FTAutoTrackType.APP_END)) {
            return;
        }
        /*设置了黑名单，且页面在其中*/
        if (FTAutoTrackConfig.get().isIgnoreAutoTrackActivity(clazz)) {
            return;
        }
        putActivityEvent(OP.CLS_ACT, clazz, false);
    }

    /**
     * 监听触摸事件
     *
     * @param view
     * @param motionEvent
     */
    public static void trackViewOnTouch(View view, MotionEvent motionEvent) {
        try {
            Object object = view.getContext();
            clickView(view, object.getClass(), AopUtils.getClassName(object), AopUtils.getSupperClassName(object), AopUtils.getViewTree(view));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 点击事件
     *
     * @param clazz
     * @param currentPage
     * @param rootPage
     * @param vtp
     */
    public static void clickView(Class<?> clazz, String currentPage, String rootPage, String vtp) {
        LogUtils.showAlias("当前点击事件的 vtp 值为:" + vtp);
        if (!FTAutoTrackConfig.get().isAutoTrack()) {
            return;
        }
        if (!FTAutoTrackConfig.get().enableAutoTrackType(FTAutoTrackType.APP_CLICK)) {
            return;
        }

        if (!FTAutoTrackConfig.get().isOnlyAutoTrackActivity(clazz)) {
            return;
        }

        if (FTAutoTrackConfig.get().disableAutoTrackType(FTAutoTrackType.APP_CLICK)) {
            return;
        }
        if (FTAutoTrackConfig.get().isIgnoreAutoTrackActivity(clazz)) {
            return;
        }
        putClickEvent(OP.CLK, currentPage, rootPage, vtp);
    }

    /**
     * 点击事件
     *
     * @param view
     * @param clazz
     * @param currentPage
     * @param rootPage
     * @param vtp
     */
    public static void clickView(View view, Class<?> clazz, String currentPage, String rootPage, String vtp) {
        LogUtils.showAlias("当前点击事件的 vtp 值为:" + vtp);
        if (!FTAutoTrackConfig.get().isAutoTrack()) {
            return;
        }
        if (!FTAutoTrackConfig.get().enableAutoTrackType(FTAutoTrackType.APP_CLICK)) {
            return;
        }
        if (!FTAutoTrackConfig.get().isOnlyAutoTrackActivity(clazz)) {
            return;
        }
        if (!FTAutoTrackConfig.get().isOnlyView(view)) {
            return;
        }

        if (FTAutoTrackConfig.get().disableAutoTrackType(FTAutoTrackType.APP_CLICK)) {
            return;
        }
        if (FTAutoTrackConfig.get().isIgnoreAutoTrackActivity(clazz)) {
            return;
        }
        if (FTAutoTrackConfig.get().isIgnoreView(view)) {
            return;
        }
        putClickEvent(OP.CLK, currentPage, rootPage, vtp);
    }

    /**
     * 记录简单事件
     *
     * @param op
     */
    public static void putSimpleEvent(@NonNull OP op) {

        ThreadPoolUtils.get().execute(() -> {
            try {


                JSONObject tags = new JSONObject();
                JSONObject fields = new JSONObject();
                Class activity = FTActivityManager.get().getLastActivity();
                if (activity != null) {
                    tags.put(Constants.KEY_PAGE_EVENT_CURRENT_PAGE_NAME, activity.getSimpleName());
                }
                SyncJsonData recordData = SyncJsonData.getFromTrackBean(new TrackBean(Constants.FT_MEASUREMENT_PAGE_EVENT, tags, fields), op);
                LogUtils.d(TAG, "FTAutoTrack数据进数据库：putSimpleEvent:" + recordData.printFormatRecordData());


                FTManager.getFTDBManager().insertFTOperation(recordData);
                FTManager.getSyncTaskManager().executeSyncPoll();
            } catch (Exception e) {
                LogUtils.e(TAG, e.toString());
            }
        });
    }

    /**
     * 记录点击事件
     *
     * @param op
     * @param currentPage
     * @param rootPage
     * @param vtp
     */
    public static void putClickEvent(@NonNull OP op, @Nullable String currentPage, @Nullable String rootPage, @Nullable String vtp) {
        long time = System.currentTimeMillis();
        putPageEvent(time, op, currentPage, rootPage, null, vtp);
    }

    /**
     * Fragment 开关
     *
     * @param op
     * @param currentPage
     * @param rootPage
     * @param parentPage
     */
    public static void putFragmentEvent(@NonNull OP op, @Nullable String currentPage, @Nullable String rootPage, @Nullable String parentPage) {
        long time = System.currentTimeMillis();
        try {
            if (op == OP.OPEN_FRA) {
                //显示Fragment的页面名称为 Activity.Fragment
                LogUtils.showAlias("当前页面的 name 值为:" + rootPage + "." + currentPage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        putPageEvent(time, op, currentPage, rootPage, parentPage, null);
    }

    /**
     * Activity 开关
     *
     * @param op
     * @param classCurrent
     */
    public static void putActivityEvent(@NonNull OP op, Class classCurrent, boolean isFirstLoad) {
        long time = System.currentTimeMillis();
        Class parentClass = FTActivityManager.get().getLastActivity();
        String parentPageName = Constants.FLOW_ROOT;
        if (op == OP.OPEN_ACT) {
            //是第一次加载 Activity ，说明其为从其他Activity 中打开
            if (isFirstLoad) {
                //如果没有上一个 Activity 说明其为 根结点
                if (parentClass != null) {
                    //判断从 上一个 页面的Activity 还是 Fragment 中打开
                    boolean isFromFragment = FTActivityManager.get().getActivityOpenFromFragment(classCurrent.getName());
                    if (isFromFragment) {
                        //从 Fragment 打开则找到上一个页面的 Fragment
                        Class c = FTFragmentManager.getInstance().getLastFragmentName(parentClass.getName());
                        if (c != null) {
                            parentPageName = parentClass.getSimpleName() + "." + c.getSimpleName();
                        } else {
                            parentPageName = parentClass.getSimpleName();
                        }
                    } else {
                        //从Activity 中打开则找到上一个Activity
                        parentPageName = parentClass.getSimpleName();
                    }
                }
            } else {
                //如果最后两个为同一个 Activity 说明 Activity 为 页面重启
                if (FTActivityManager.get().lastTwoActivitySame()) {
                    parentPageName = classCurrent.getSimpleName();
                } else {
                    //如果不相等，表示从其他返回过来
                    if (parentClass != null) {
                        boolean isFromFragment = FTActivityManager.get().getActivityOpenFromFragment(parentClass.getName());
                        if (isFromFragment) {
                            //这部分需要创建一条子页面的数据
                            Class lastFragment = FTFragmentManager.getInstance().getLastFragmentName(classCurrent.getName());
                            if (lastFragment != null) {
                                parentPageName = Constants.MOCK_SON_PAGE_DATA + ":" + lastFragment.getSimpleName() + ":" + parentClass.getSimpleName();
                            } else {
                                parentPageName = Constants.MOCK_SON_PAGE_DATA + ":" + parentClass.getSimpleName();
                            }
                        } else {
                            //从Activity 中打开则找到上一个Activity
                            parentPageName = parentClass.getSimpleName();
                        }
                    }
                }
            }
        }
        try {
            if (op == OP.OPEN_ACT) {
                LogUtils.showAlias("当前页面的 name 值为:" + classCurrent.getSimpleName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        putPageEvent(time, op, classCurrent.getSimpleName(), classCurrent.getSimpleName(), parentPageName, null);
    }

    private static void putPageEvent(long time, @NonNull OP op, @Nullable String currentPage,
                                     @Nullable String rootPage, @Nullable String parentPage, @Nullable String vtp) {
        try {

            JSONObject tags = new JSONObject();
            JSONObject fields = new JSONObject();
            if (rootPage != null) {
                tags.put(Constants.KEY_PAGE_EVENT_ROOT_PAGE_NAME, rootPage);
            }
            if (currentPage != null) {

                tags.put(Constants.KEY_PAGE_EVENT_CURRENT_PAGE_NAME, currentPage);
            }
            if (vtp != null) {
                tags.put("vtp", vtp);
                fields.put("vtp_desc", FTAliasConfig.get().getVtpDesc(vtp));
                fields.put("vtp_id", Utils.MD5(vtp));
            }
            FTTrackInner.getInstance().trackBackground(op, time, Constants.FT_MEASUREMENT_PAGE_EVENT, tags, fields);

            addLogging(currentPage, op, vtp);
            addObject(op);

        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
        }
    }

    /**
     * WebView 数据加载收集
     *
     * @param time
     * @param op
     * @param url
     * @param duration
     */
    public static void putWebViewTimeCost(long time, OP op, String url, long duration) {
        try {
            JSONObject tags = new JSONObject();
            JSONObject fields = new JSONObject();

            tags.put(Constants.KEY_TIME_COST_WEBVIEW_URL, url);
            tags.put(Constants.KEY_TIME_COST_DURATION, duration);

            FTTrackInner.getInstance().trackBackground(op, time, Constants.FT_MEASUREMENT_TIME_COST_WEBVIEW, tags, fields);

        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
        }
    }

    /**
     * 记录应用消耗时间
     *
     * @param time
     * @param op
     * @param duration
     */
    private static void putClientTimeCost(long time, OP op, long duration) {
        try {
            JSONObject tags = new JSONObject();
            JSONObject fields = new JSONObject();
            fields.put(Constants.KEY_TIME_COST_DURATION, duration);

            FTTrackInner.getInstance().trackBackground(op, time, Constants.FT_MEASUREMENT_TIME_COST_CLIENT, tags, fields);
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
        }
    }

    /**
     * http 请求统计
     *
     * @param time
     * @param op
     * @param url
     */
    public static void putHttpError(long time, OP op, String url, boolean isError) {

        try {
            JSONObject tags = new JSONObject();
            JSONObject fields = new JSONObject();

            fields.put(Constants.KEY_HTTP_URL, url);
            fields.put(Constants.KEY_HTTP_IS_ERROR, isError ? 1 : 0);

            String measurement = "";
            if (op.equals(OP.HTTP_WEBVIEW)) {
                measurement = Constants.FT_MEASUREMENT_HTTP_WEBVIEW;
            } else if (op.equals(OP.HTTP_CLIENT)) {
                measurement = Constants.FT_MEASUREMENT_HTTP_CLIENT;
            }

            FTTrackInner.getInstance().trackBackground(op, time, measurement, tags, fields);
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
        }
    }

    /**
     * 应用登陆状态，添加
     *
     * @param op
     */
    private static void addObject(OP op) {
        if (op != OP.LANC) {
            return;
        }
        Context context = FTApplication.getApplication();
        String name = DeviceUtils.getUuid(context);
        ObjectBean objectBean = new ObjectBean(name, Constants.DEFAULT_OBJECT_CLASS, SyncDataHelper.getDefaultObjectBean());
        FTTrackInner.getInstance().objectBackground(objectBean);
    }


    private static void addLogging(String currentPage, OP op, @Nullable String vtp) {
        addLogging(currentPage, op, 0, vtp);
    }

    private static void addLogging(String currentPage, OP op, long duration, @Nullable String vtp) {
        if (!FTFlowConfig.get().isEventFlowLog()) {
            return;
        }

        if (!op.equals(OP.CLK)
                && !op.equals(OP.LANC)
                && !op.equals(OP.OPEN_ACT)
                && !op.equals(OP.OPEN_FRA)
                && !op.equals(OP.CLS_ACT)
                && !op.equals(OP.CLS_FRA)
                && !op.equals(OP.OPEN)) {
            return;
        }
        String operationName = "";
        String event = "";
        switch (op) {
            case CLK:
                event = Constants.EVENT_NAME_CLICK;
                break;
            case LANC:
                event = Constants.EVENT_NAME_LAUNCH;
                break;
            case OPEN_ACT:
            case OPEN_FRA:
                event = Constants.EVENT_NAME_ENTER;
                break;
            case CLS_ACT:
            case CLS_FRA:
                event = Constants.EVENT_NAME_LEAVE;
                break;
            case OPEN:
                event = Constants.EVENT_NAME_OPEN;
                break;
        }
        operationName = event + "/event";
        JSONObject content = new JSONObject();
        try {
            if (currentPage != null) {
                content.put("current_page_name", currentPage);
            }
            content.put("event", event);
            if (vtp != null) {
                content.put("view_tree_path", vtp);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogBean logBean = new LogBean(content.toString(), System.currentTimeMillis());
        logBean.setOperationName(operationName);
        logBean.setDuration(duration);
        FTTrackInner.getInstance().logBackground(logBean);
    }

    /**
     * 获取相应埋点方法（Activity）所执行的时间(该方法会在所有的继承了 AppCompatActivity 的 Activity 中的 onCreate 中调用)
     *
     * @param desc className + "|" + methodName + "|" + methodDesc
     * @param cost
     */
    public static void timingMethod(String desc, long cost) {
        LogUtils.d(TAG, desc);
        try {
            String[] arr = desc.split("\\|");
            String[] names = arr[0].split("/");
            String pageName = names[names.length - 1];
            addLogging(pageName, OP.OPEN, cost * 1000, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 插桩方法用来替换调用的 android/webkit/webView.loadUrl 方法，该方法结构谨慎修改，修该后请同步修改
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] 类中的 visitMethodInsn 方法中关于该替换内容的部分
     *
     * @param webView
     * @param url
     * @return
     */

    public static void loadUrl(View webView, String url) {
        if (webView == null) {
            throw new NullPointerException("WebView has not initialized.");
        }
        setUpWebView(webView);
        invokeWebViewLoad(webView, "loadUrl", new Object[]{url}, new Class[]{String.class});
    }

    /**
     * 插桩方法用来替换调用的 android/webkit/webView.loadUrl 方法，该方法结构谨慎修改，修该后请同步修改
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] 类中的 visitMethodInsn 方法中关于该替换内容的部分
     *
     * @param webView
     * @param url
     * @param additionalHttpHeaders
     * @return
     */
    public static void loadUrl(View webView, String url, Map<String, String> additionalHttpHeaders) {
        if (webView == null) {
            throw new NullPointerException("WebView has not initialized.");
        }
        setUpWebView(webView);
        invokeWebViewLoad(webView, "loadUrl", new Object[]{url, additionalHttpHeaders}, new Class[]{String.class, Map.class});
    }

    /**
     * 插桩方法用来替换调用的 android/webkit/webView.loadData 方法，该方法结构谨慎修改，修该后请同步修改
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] 类中的 visitMethodInsn 方法中关于该替换内容的部分
     *
     * @param webView
     * @param data
     * @param encoding
     * @param mimeType
     * @return
     */
    public static void loadData(View webView, String data, String mimeType, String encoding) {
        if (webView == null) {
            throw new NullPointerException("WebView has not initialized.");
        }
        setUpWebView(webView);
        invokeWebViewLoad(webView, "loadData", new Object[]{data, mimeType, encoding}, new Class[]{String.class, String.class, String.class});
    }

    /**
     * 插桩方法用来替换调用的 android/webkit/webView.loadDataWithBaseURL 方法，该方法结构谨慎修改，修该后请同步修改
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] 类中的 visitMethodInsn 方法中关于该替换内容的部分
     *
     * @param webView
     * @param data
     * @param encoding
     * @param mimeType
     * @return
     */
    public static void loadDataWithBaseURL(View webView, String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        if (webView == null) {
            throw new NullPointerException("WebView has not initialized.");
        }
        setUpWebView(webView);
        invokeWebViewLoad(webView, "loadDataWithBaseURL", new Object[]{baseUrl, data, mimeType, encoding, historyUrl},
                new Class[]{String.class, String.class, String.class, String.class, String.class});
    }

    /**
     * 插桩方法用来替换调用的 android/webkit/webView.postUrl 方法，该方法结构谨慎修改，修该后请同步修改
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] 类中的 visitMethodInsn 方法中关于该替换内容的部分
     *
     * @param webView
     * @param url
     * @return
     */
    public static void postUrl(View webView, String url, byte[] postData) {
        if (webView == null) {
            throw new NullPointerException("WebView has not initialized.");
        }
        setUpWebView(webView);
        invokeWebViewLoad(webView, "postUrl", new Object[]{url, postData},
                new Class[]{String.class, byte[].class});
    }

    private static void setUpWebView(View webView) {
        if (webView instanceof WebView) {
            ((WebView) webView).setWebViewClient(new FTWebViewClient());
        }
    }

    private static void invokeWebViewLoad(View webView, String methodName, Object[] params, Class[] paramTypes) {
        try {
            Class<?> clazz = webView.getClass();
            Method loadMethod = clazz.getMethod(methodName, paramTypes);
            loadMethod.invoke(webView, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 插桩方法用来替换调用的 OkHttpClient.Builder.build() 方法，该方法结构谨慎修改，修该后请同步修改
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] 类中的 visitMethodInsn 方法中关于该替换内容的部分
     *
     * @param builder
     * @return
     */
    public static OkHttpClient trackOkHttpBuilder(OkHttpClient.Builder builder) {
        builder.addInterceptor(new FTNetWorkTracerInterceptor());
        return builder.build();
    }

    /**
     * 插桩方法用来替换调用的 org/apache/hc/client5/http/impl/classic/HttpClientBuilder.build() 方法，该方法结构谨慎修改，修该后请同步修改
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] 类中的 visitMethodInsn 方法中关于该替换内容的部分
     *
     * @param builder
     * @return
     */
    public static CloseableHttpClient trackHttpClientBuilder(HttpClientBuilder builder) {
        FTHttpClientInterceptor interceptor = new FTHttpClientInterceptor();
        builder.addRequestInterceptorFirst(new FTHttpClientRequestInterceptor(interceptor));
        builder.addResponseInterceptorLast(new FTHttpClientResponseInterceptor(interceptor));
        return builder.build();
    }
}
