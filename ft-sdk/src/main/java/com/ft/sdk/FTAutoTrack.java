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

import com.ft.sdk.garble.FTAutoTrackConfigManager;
import com.ft.sdk.garble.FTFragmentManager;
import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.utils.AopUtils;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.NetUtils;
import com.ft.sdk.garble.utils.OaidUtils;
import com.ft.sdk.garble.utils.Utils;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

/**
 * BY huangDianHua
 * DATE:2019-12-02 16:43
 * Description
 */
public class FTAutoTrack {
    public final static String TAG = "FTAutoTrack";
    public final static String WEBVIEW_HANDLED_FLAG = "webview_auto_track_handled";

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
     * @deprecated 该方法原来被 FT Plugin 插件调用，目前不再使用。目前监控应用的启动使用{@link #startPage(Class)}方法
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
            clickView(tab.view, object.getClass(), AopUtils.getClassName(object),
                    AopUtils.getSupperClassName(object), AopUtils.getViewDesc(tab.view) + "#pos:" + tab.getPosition());
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
                clickView(view, object.getClass(), AopUtils.getClassName(object),
                        AopUtils.getSupperClassName(object), AopUtils.getViewDesc(view));
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


    /**
     * APP 启动
     */
    public static void startApp() {
        if (!FTAutoTrackConfigManager.get().isAutoTrack()) {
            return;
        }
        if (!FTAutoTrackConfigManager.get().enableAutoTrackType(FTAutoTrackType.APP_START)) {
            return;
        }
        putPageEvent(Utils.getCurrentNanoTime(), OP.LANC, null, null, null, null);
    }


    /**
     * 应用休眠
     *
     * @param timeDelayMs
     */
    public static void sleepApp(long timeDelayMs, int startTimeline) {
        long now = Utils.getCurrentNanoTime() - timeDelayMs * 1000000;
        putClientTimeCost(now, now - startTimeline);

    }


    /**
     * 打开某个Fragment页面
     *
     * @param clazz
     * @param activity
     */
    public static void startPage(Object clazz, Object activity, String parentPage) {
        /*没有开启自动埋点*/
        if (!FTAutoTrackConfigManager.get().isAutoTrack()) {
            return;
        }
        /*设置了白名单，但当前事件不在其中*/
        if (!FTAutoTrackConfigManager.get().enableAutoTrackType(FTAutoTrackType.APP_START)) {
            return;
        }

        /*设置了白名单，但当前页面不在其中*/
        if (!FTAutoTrackConfigManager.get().isOnlyAutoTrackActivity(activity.getClass())) {
            return;
        }
        /*设置了白名单，但当前页面不在其中*/
        if (!FTAutoTrackConfigManager.get().isOnlyAutoTrackActivity((Class<?>) clazz)) {
            return;
        }
        /*设置了黑名单，且事件在其中*/
        if (FTAutoTrackConfigManager.get().disableAutoTrackType(FTAutoTrackType.APP_START)) {
            return;
        }
        /*设置了黑名单，且页面在其中*/
        if (FTAutoTrackConfigManager.get().isIgnoreAutoTrackActivity(activity.getClass())) {
            return;
        }
        /*设置了黑名单，且页面在其中*/
        if (FTAutoTrackConfigManager.get().isIgnoreAutoTrackActivity((Class<?>) clazz)) {
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
        if (!FTAutoTrackConfigManager.get().isAutoTrack()) {
            return;
        }
        /*设置了白名单，但当前事件不在其中*/
        if (!FTAutoTrackConfigManager.get().enableAutoTrackType(FTAutoTrackType.APP_END)) {
            return;
        }

        /*设置了白名单，但当前页面不在其中*/
        if (!FTAutoTrackConfigManager.get().isOnlyAutoTrackActivity(activity.getClass())) {
            return;
        }
        /*设置了白名单，但当前页面不在其中*/
        if (!FTAutoTrackConfigManager.get().isOnlyAutoTrackActivity((Class<?>) clazz)) {
            return;
        }
        /*设置了黑名单，且事件在其中*/
        if (FTAutoTrackConfigManager.get().disableAutoTrackType(FTAutoTrackType.APP_END)) {
            return;
        }
        /*设置了黑名单，且页面在其中*/
        if (FTAutoTrackConfigManager.get().isIgnoreAutoTrackActivity(activity.getClass())) {
            return;
        }
        /*设置了黑名单，且页面在其中*/
        if (FTAutoTrackConfigManager.get().isIgnoreAutoTrackActivity((Class<?>) clazz)) {
            return;
        }
        putFragmentEvent(OP.CLS_FRA, AopUtils.getClassName(clazz), AopUtils.getActivityName(activity), parentPage);
    }

    /**
     * 打开某个Activity页面
     *
     * @param clazz
     */
    public static void startPage(Class<?> clazz) {
        /*没有开启自动埋点*/
        if (!FTAutoTrackConfigManager.get().isAutoTrack()) {
            return;
        }
        /*设置了白名单，但当前事件不在其中*/
        if (!FTAutoTrackConfigManager.get().enableAutoTrackType(FTAutoTrackType.APP_START)) {
            return;
        }
        /*设置了白名单，但当前页面不在其中*/
        if (!FTAutoTrackConfigManager.get().isOnlyAutoTrackActivity(clazz)) {
            return;
        }
        /*设置了黑名单，且事件在其中*/
        if (FTAutoTrackConfigManager.get().disableAutoTrackType(FTAutoTrackType.APP_START)) {
            return;
        }
        /*设置了黑名单，且页面在其中*/
        if (FTAutoTrackConfigManager.get().isIgnoreAutoTrackActivity(clazz)) {
            return;
        }
        putActivityEvent(OP.OPEN_ACT, clazz);
    }

    /**
     * 关闭某个Activity
     *
     * @param clazz
     */
    public static void destroyPage(Class<?> clazz) {
        /*没有开启自动埋点*/
        if (!FTAutoTrackConfigManager.get().isAutoTrack()) {
            return;
        }
        /*设置了白名单，但当前事件不在其中*/
        if (!FTAutoTrackConfigManager.get().enableAutoTrackType(FTAutoTrackType.APP_END)) {
            return;
        }
        /*设置了白名单，但当前页面不在其中*/
        if (!FTAutoTrackConfigManager.get().isOnlyAutoTrackActivity(clazz)) {
            return;
        }
        /*设置了黑名单，且事件在其中*/
        if (FTAutoTrackConfigManager.get().disableAutoTrackType(FTAutoTrackType.APP_END)) {
            return;
        }
        /*设置了黑名单，且页面在其中*/
        if (FTAutoTrackConfigManager.get().isIgnoreAutoTrackActivity(clazz)) {
            return;
        }
        putActivityEvent(OP.CLS_ACT, clazz);
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
            clickView(view, object.getClass(), AopUtils.getClassName(object),
                    AopUtils.getSupperClassName(object), AopUtils.getViewDesc(view));
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
        if (!FTAutoTrackConfigManager.get().isAutoTrack()) {
            return;
        }
        if (!FTAutoTrackConfigManager.get().enableAutoTrackType(FTAutoTrackType.APP_CLICK)) {
            return;
        }

        if (!FTAutoTrackConfigManager.get().isOnlyAutoTrackActivity(clazz)) {
            return;
        }

        if (FTAutoTrackConfigManager.get().disableAutoTrackType(FTAutoTrackType.APP_CLICK)) {
            return;
        }
        if (FTAutoTrackConfigManager.get().isIgnoreAutoTrackActivity(clazz)) {
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
        if (!FTAutoTrackConfigManager.get().isAutoTrack()) {
            return;
        }
        if (!FTAutoTrackConfigManager.get().enableAutoTrackType(FTAutoTrackType.APP_CLICK)) {
            return;
        }
        if (!FTAutoTrackConfigManager.get().isOnlyAutoTrackActivity(clazz)) {
            return;
        }
        if (!FTAutoTrackConfigManager.get().isOnlyView(view)) {
            return;
        }

        if (FTAutoTrackConfigManager.get().disableAutoTrackType(FTAutoTrackType.APP_CLICK)) {
            return;
        }
        if (FTAutoTrackConfigManager.get().isIgnoreAutoTrackActivity(clazz)) {
            return;
        }
        if (FTAutoTrackConfigManager.get().isIgnoreView(view)) {
            return;
        }
        putClickEvent(OP.CLK, currentPage, rootPage, vtp);
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
        long time = Utils.getCurrentNanoTime();
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
        long time = Utils.getCurrentNanoTime();
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
    public static void putActivityEvent(@NonNull OP op, Class classCurrent) {
        long time = Utils.getCurrentNanoTime();
        Class parentClass = FTActivityManager.get().getLastActivity();
        String parentPageName = Constants.FLOW_ROOT;
        if (op == OP.OPEN_ACT) {
            //是第一次加载 Activity ，说明其为从其他Activity 中打开
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
//            if (vtp != null) {
//                tags.put("vtp", vtp);
//                fields.put("vtp_desc", FTAliasConfig.get().getVtpDesc(vtp));
//                fields.put("vtp_id", Utils.MD5(vtp));
//            }
//
//            if (!Utils.isNullOrEmpty(currentPage)) {
//                //如果是 Fragment 就把Activity 的名称也添加上去
//                if (op.equals(OP.OPEN_FRA) || op.equals(OP.CLS_FRA)) {
//                    tags.put(Constants.KEY_PAGE_EVENT_CURRENT_PAGE_NAME, rootPage + "." + currentPage);
//                    fields.put(Constants.KEY_PAGE_EVENT_PAGE_DESC, FTAliasConfig.get().getPageDesc(rootPage + "." + currentPage));
//
//                } else {
//                    tags.put(Constants.KEY_PAGE_EVENT_CURRENT_PAGE_NAME, currentPage);
//                    fields.put(Constants.KEY_PAGE_EVENT_PAGE_DESC, FTAliasConfig.get().getPageDesc(currentPage));
//                }
//            }
//
//            String eventName = op.toEventName();
//            fields.put(Constants.KEY_EVENT, eventName);
//            tags.put(Constants.KEY_EVENT_ID, Utils.MD5(eventName));

//            if(op.needMonitorData()){
//                SyncDataHelper.addMonitorData(tags,fields);
//            }

            handleOp(currentPage, op, parentPage, vtp);

        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
        }
    }

    /**
     * 记录应用登陆时效
     */
    public static void putRUMLaunchPerformance(boolean isCold, long duration) {
        FTRUMGlobalManager.get().addAction(
                isCold ? "app cold start" : "app hot start", isCold ? "launch_cold" : "launch_hot", duration);
    }

    /**
     * 记录页面加载性能
     */
    public static void putRUMViewLoadPerformance(String viewName, long loadTime) {
        FTRUMGlobalManager.get().onCreateView(viewName, loadTime);
    }


    /**
     * 记录应用消耗时间
     *
     * @param time
     * @param duration
     */
    private static void putClientTimeCost(long time, long duration) {
        try {
            JSONObject tags = new JSONObject();
            JSONObject fields = new JSONObject();
            fields.put(Constants.KEY_TIME_COST_DURATION, duration * 1000);

            FTTrackInner.getInstance().trackBackground(time, Constants.FT_MEASUREMENT_TIME_COST_CLIENT, tags, fields);
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
        }
    }

    /**
     * 获取变化的公用 tag
     *
     * @return
     */
    static JSONObject getRUMPublicTags() throws Exception {
        JSONObject tags = new JSONObject();
        FTRUMConfig config = FTRUMConfigManager.get().getConfig();
        HashMap<String, String> globalContext = config.getGlobalContext();
        ArrayList<String> customKeys = new ArrayList<>();
        for (Map.Entry<String, String> entry : globalContext.entrySet()) {
            String key = entry.getKey();
            customKeys.add(key);
            Object value = entry.getValue();
            tags.put(key, value.toString());
        }
        tags.put(Constants.KEY_RUM_CUSTOM_KEYS, new Gson().toJson(customKeys));
        tags.put(Constants.KEY_RUM_APP_ID, config.getRumAppId());
        tags.put(Constants.KEY_RUM_SDK_NAME, Constants.SDK_NAME);
        tags.put(Constants.KEY_RUM_ENV, FTSdk.get().getBaseConfig().getEnv().toString());
        tags.put(Constants.KEY_RUM_NETWORK_TYPE, NetUtils.get().getNetWorkStateName());
        tags.put(Constants.KEY_RUM_IS_SIGNIN, FTRUMConfigManager.get().isUserDataBinded() ? "T" : "F");
        if (FTRUMConfigManager.get().isUserDataBinded()) {
            tags.put(Constants.KEY_RUM_USER_ID, FTRUMConfigManager.get().getUserData().getId());
        } else {
            tags.put(Constants.KEY_RUM_USER_ID, FTRUMGlobalManager.get().getSessionId());
        }

        String uuid = "";
        if (FTHttpConfigManager.get().useOaid) {
            String oaid = OaidUtils.getOAID(FTApplication.getApplication());
            if (oaid != null) {
                uuid = "oaid_" + oaid;
            }
        }
        if (uuid.isEmpty()) {
            uuid = DeviceUtils.getUuid(FTApplication.getApplication());
        }
        tags.put(Constants.KEY_DEVICE_UUID, uuid);
        tags.put(Constants.KEY_RUM_SESSION_TYPE, "user");
        return tags;

    }

    private static void handleOp(String currentPage, OP op, String parentPage, @Nullable String vtp) {
        handleOp(currentPage, op, parentPage, 0, vtp);
    }

    private static void handleOp(String currentPage, OP op, String parentPage, long duration, @Nullable String vtp) {
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        if (!manager.isRumEnable()) {
            return;
        }
        if (!op.equals(OP.CLK)
                && !op.equals(OP.LANC)
                && !op.equals(OP.OPEN_ACT)
                && !op.equals(OP.OPEN_FRA)
                && !op.equals(OP.CLS_ACT)
                && !op.equals(OP.CLS_FRA)
            //                && !op.equals(OP.OPEN))

        ) {
            return;
        }
        if (op.equals(OP.CLK) && !manager.getConfig().isEnableTraceUserAction()) {
            return;
        }

        if ((op.equals(OP.OPEN_ACT) || op.equals(OP.CLS_ACT))
                && !manager.getConfig().isEnableTraceUserView()) {
            return;
        }


        String event = "";
        switch (op) {
            case CLK:
                event = Constants.EVENT_NAME_CLICK;

                FTRUMGlobalManager.get().startAction(vtp, event);
                break;
            case LANC:
                event = Constants.EVENT_NAME_LAUNCH;
                break;
            case OPEN_ACT:
//            case OPEN_FRA:
                event = Constants.EVENT_NAME_ENTER;
                FTRUMGlobalManager.get().startView(currentPage, parentPage);
                break;
            case CLS_ACT:
//            case CLS_FRA:
                event = Constants.EVENT_NAME_LEAVE;
                FTRUMGlobalManager.get().stopView();
                break;
            case OPEN:
                event = Constants.EVENT_NAME_OPEN;
                break;
        }

    }

    /**
     * 获取相应埋点方法（Activity）所执行的时间(该方法会在所有的继承了 AppCompatActivity 的 Activity 中的 onCreate 中调用)
     *
     * @param desc className + "|" + methodName + "|" + methodDesc
     * @param cost
     */
    public static void timingMethod(String desc, long cost) {
//        LogUtils.d(TAG, desc);
        try {
            String[] arr = desc.split("\\|");
            String[] names = arr[0].split("/");
            String pageName = names[names.length - 1];
            handleOp(pageName, OP.OPEN, null, cost * 1000, null);
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
        if (webView instanceof WebView && webView.getTag(R.id.ft_webview_handled_tag_view_value) == null) {

            if (FTTraceConfigManager.get().isEnableWebTrace()) {
                ((WebView) webView).setWebViewClient(new FTWebViewClient());
            }
            new FTWebViewHandler().setWebView((WebView) webView);
            webView.setTag(R.id.ft_webview_handled_tag_view_value, "handled");
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
        if (FTTraceConfigManager.get().isEnableAutoTrace()) {
            builder.addInterceptor(new FTTraceInterceptor());
        }
//            builder.addNetworkInterceptor(interceptor); //发现部分工程有兼容问题
        if (FTRUMConfigManager.get().isRumEnable()) {
            if (FTRUMConfigManager.get().getConfig().isEnableTraceUserResource()) {
                FTResourceInterceptor interceptor = new FTResourceInterceptor();
                builder.addInterceptor(interceptor);
                builder.eventListener(interceptor);
            }
        }
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
        if (FTTraceConfigManager.get().isEnableAutoTrace()) {
            builder.addRequestInterceptorFirst(new FTHttpClientRequestInterceptor(interceptor));
            builder.addResponseInterceptorLast(new FTHttpClientResponseInterceptor(interceptor));
        }
        return builder.build();
    }
}
