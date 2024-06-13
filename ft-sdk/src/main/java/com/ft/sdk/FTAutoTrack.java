package com.ft.sdk;

import static com.ft.sdk.FTApplication.getApplication;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
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
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.utils.AopUtils;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageUtils;
import com.ft.sdk.garble.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * BY huangDianHua
 * DATE:2019-12-02 16:43
 * Description
 */
public class FTAutoTrack {
    public final static String TAG = Constants.LOG_TAG_PREFIX + "AutoTrack";


    /**
     * 启动 APP
     * 警告！！！该方法不能删除
     * <p>
     * 该方法原来被 FT Plugin 插件调用
     */
    public static void startApp(Application app) {
        try {
            LogUtils.d(TAG, "startApp:" + app);
            //判断是否为主进程
            if (Utils.isMainProcess()) {
                FTActivityLifecycleCallbacks life = new FTActivityLifecycleCallbacks();

                if (app != null) {
                    Class<?> clazz = PackageUtils.getSophixClass();
                    if (clazz == null || !clazz.isInstance(app)) {
                        app.registerActivityLifecycleCallbacks(life);
                        //排除在后台被启动的情况
                        if (Utils.isAppForeground()) {
                            FTAppStartCounter.get().markCodeStartTimeLine();
                        }
                    }

                } else {
                    getApplication().registerActivityLifecycleCallbacks(life);

                    //排除在后台被启动的情况
                    if (Utils.isAppForeground()) {
                        FTAppStartCounter.get().markCodeStartTimeLine();
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));
        }
    }

    /**
     * 启动 APP
     * 警告！！！该方法不能删除
     * <p>
     * 该方法原来被 FT Plugin 插件调用
     * 兼容 ft-plugin:1.2.0-beta03 之前的版本
     */
    public static void startApp(Object object) {
        LogUtils.d(TAG, "invoke compatible start app");
        startApp(null);
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
//        try {
//            if (intent != null && intent.getComponent() != null) {
//                FTActivityManager.get().putActivityOpenFromFragment(intent.getComponent().getClassName(), fromFragment);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Activity 开启
     * 警告！！！该方法不能删除
     *
     * @deprecated 该方法原来被 FT Plugin 插件调用，目前不再使用。
     */
    @Deprecated
    public static void activityOnCreate(Class clazz) {
        try {
            //startPage(clazz);
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));

        }
    }


    /**
     * Activity 关闭
     * 警告！！！该方法不能删除
     *
     * @deprecated 该方法原来被 FT Plugin 插件调用，目前不再使用。
     */
    @Deprecated
    public static void activityOnDestroy(Class clazz) {
        try {
            //destroyPage(clazz);
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));

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
//        try {
//            String className;
//            if (activity == null) {
//                Activity activity1 = FTActivityManager.get().getTopActivity();
//                className = activity1.getClass().getName();
//            } else {
//                className = activity.getClass().getName();
//            }
//            FTFragmentManager.getInstance().setFragmentVisible(className, (Class) clazz, isVisible);
//        } catch (Exception e) {
//        }
    }

    /**
     * Fragment 打开
     * 警告！！！该方法不能删除
     *
     * @param clazz
     * @param activity
     * @deprecated 该方法原来被 FT Plugin 插件调用，目前不再使用。
     */
    @Deprecated
    public static void fragmentOnResume(Object clazz, Object activity) {
        try {
            //LogUtils.d("Fragment[\nOnCreateView=====>fragment:"+((Class)clazz).getSimpleName());
            //startPage(clazz, activity);
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));

        }
    }

    /**
     * Fragment 关闭
     * 警告！！！该方法不能删除
     *
     * @param clazz
     * @param activity
     * @deprecated 该方法原来被 FT Plugin 插件调用，目前不再使用。
     */
    @Deprecated
    public static void fragmentOnPause(Object clazz, Object activity) {
        try {
            //LogUtils.d("Fragment[\nOnDestroyView=====>fragment:"+((Class)clazz).getSimpleName());
            //destroyPage(clazz, activity);
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));

        }
    }

    /**
     * 点击事件
     * 警告！！！该方法不能删除
     *
     * @param view 点击触发的页面 {@link View}
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
    public static void trackTabLayoutSelected(Object tab) {
        try {
            Field viewField = tab.getClass().getField("view");
            View view = (View) viewField.get(tab);
            Method getPosition = tab.getClass().getMethod("getPosition");
            int position = (int) getPosition.invoke(tab);
            Object object = view.getContext();
            clickView(view, AopUtils.getClass(object), AopUtils.getClassName(object),
                    AopUtils.getSupperClassName(object), AopUtils.getViewDesc(view) + "#pos:" + position);
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));

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
            LogUtils.e(TAG, Log.getStackTraceString(e));

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
                clickView(view, AopUtils.getClass(object), AopUtils.getClassName(object),
                        AopUtils.getSupperClassName(object), AopUtils.getViewDesc(view));
            }
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));
        }
    }

    /**
     * {@link MenuItem} 点击事件
     *
     * @param menuItem
     */
    public static void trackMenuItem(MenuItem menuItem) {
        try {
            trackMenuItem(null, menuItem);
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));

        }
    }

    /**
     * {@link MenuItem} 点击事件
     *
     * @param object
     * @param menuItem
     */
    public static void trackMenuItem(Object object, MenuItem menuItem) {
        try {
            clickView((Class<?>) object, AopUtils.getClassName(object), AopUtils.getSupperClassName(object),
                    AopUtils.getMenuItem(menuItem));
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));

        }
    }

    /**
     * 对话框事件监听
     *
     * @param dialogInterface
     * @param whichButton
     */
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
            LogUtils.e(TAG, Log.getStackTraceString(e));

        }
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
            clickView(view, AopUtils.getClass(object), AopUtils.getClassName(object),
                    AopUtils.getSupperClassName(object), AopUtils.getViewDesc(view));
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));

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

        FTRUMConfigManager manager = FTRUMConfigManager.get();
        if (!manager.isRumEnable()) {
            return;
        }
        if (!op.equals(OP.CLK)) {
            return;
        }
        //config nonnull here ignore warning
        if (!manager.getConfig().isEnableTraceUserAction()) {
            return;
        }

        FTRUMInnerManager.get().startAction(vtp, Constants.EVENT_NAME_CLICK);
    }


    /**
     * 记录应用登陆时效
     */
    public static void putRUMLaunchPerformance(boolean isCold, long duration, long startTime) {
        FTRUMInnerManager.get().addAction(
                isCold ? Constants.ACTION_NAME_LAUNCH_COLD : Constants.ACTION_NAME_LAUNCH_HOT,
                isCold ? Constants.ACTION_TYPE_LAUNCH_COLD : Constants.ACTION_TYPE_LAUNCH_HOT,
                duration, startTime);
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
//            String[] arr = desc.split("\\|");
//            String[] names = arr[0].split("/");
//            String pageName = names[names.length - 1];
//            handleOp(pageName, OP.OPEN, cost * 1000, null);
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));

        }
    }

    /**
     * 插桩方法用来替换调用的 {@link android.webkit.WebView#loadUrl(String)} 方法，该方法结构谨慎修改，修该后请同步修改
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
     * 插桩方法用来替换调用的 {@link android.webkit.WebView#loadUrl(String)} 方法，该方法结构谨慎修改，修该后请同步修改
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
     * 插桩方法用来替换调用的 {@link android.webkit.WebView#loadData(String, String, String)}方法，该方法结构谨慎修改，修该后请同步修改
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
     * 插桩方法用来替换调用的 {@link android.webkit.WebView#loadDataWithBaseURL(String, String, String, String, String)} 方法，该方法结构谨慎修改，修该后请同步修改
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
     * 插桩方法用来替换调用的 {@link android.webkit.WebView#postUrl(String, byte[])} } 方法，该方法结构谨慎修改，修该后请同步修改
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

    /**
     * 设置 webview 监听事件
     *
     * @param webView
     */
    public static void setUpWebView(View webView) {
        if (webView instanceof WebView && webView.getTag(R.id.ft_webview_handled_tag_view_value) == null) {

            if (FTTraceConfigManager.get().isEnableWebTrace()) {
                ((WebView) webView).setWebViewClient(new FTWebViewClient());
            }
            new FTWebViewHandler().setWebView((WebView) webView);
            webView.setTag(R.id.ft_webview_handled_tag_view_value, "handled");
        }
    }

    /**
     * 调用 webview 方法
     *
     * @param webView
     * @param methodName 方法名
     * @param params     参数
     * @param paramTypes
     */
    private static void invokeWebViewLoad(View webView, String methodName, Object[] params, Class[] paramTypes) {
        try {
            Class<?> clazz = webView.getClass();
            Method loadMethod = clazz.getMethod(methodName, paramTypes);
            loadMethod.invoke(webView, params);
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));

        }
    }

    /**
     * 插桩方法用来替换调用的 {@link OkHttpClient.Builder#build() }方法，该方法结构谨慎修改，修该后请同步修改
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] 类中的 visitMethodInsn 方法中关于该替换内容的部分
     *
     * @param builder
     * @return
     */
    public static OkHttpClient trackOkHttpBuilder(OkHttpClient.Builder builder) {
        LogUtils.d(TAG, "trackOkHttpBuilder");

//            builder.addNetworkInterceptor(interceptor); //发现部分工程有兼容问题
        if (FTRUMConfigManager.get().isRumEnable()) {
            FTRUMConfig config = FTRUMConfigManager.get().getConfig();
            //config nonnull here ignore warning
            if (config.isEnableTraceUserResource()) {
                boolean hasSetResource = false;//是否已经设置 FTResourceInterceptor
                for (Interceptor interceptor : builder.interceptors()) {
                    if (interceptor instanceof FTResourceInterceptor) {
                        hasSetResource = true;
                        break;
                    }

                }
                if (!hasSetResource) {
                    builder.interceptors().add(0, new FTResourceInterceptor());
                }
                FTResourceEventListener.FTFactory factory = FTRUMConfigManager.get().getOverrideEventListener();
                if (factory != null) {
                    builder.eventListenerFactory(factory);
                } else {
                    builder.eventListenerFactory(new FTResourceEventListener.FTFactory(config.isEnableResourceHostIP()));
                }
            }
        }

        if (FTTraceConfigManager.get().isEnableAutoTrace()) {
            boolean hasSetTrace = false;//是否已经设置 FTResourceInterceptor

            for (Interceptor interceptor : builder.interceptors()) {
                if (interceptor instanceof FTTraceInterceptor) {
                    hasSetTrace = true;
                    break;
                }

            }
            if (!hasSetTrace) {
                builder.interceptors().add(0, new FTTraceInterceptor());
            }
        }

        return builder.build();
    }


}
