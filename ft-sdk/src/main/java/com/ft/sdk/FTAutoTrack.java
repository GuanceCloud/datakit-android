package com.ft.sdk;

import static com.ft.sdk.FTApplication.getApplication;

import android.app.Activity;
import android.app.Application;
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
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.ResourceID;
import com.ft.sdk.garble.utils.AopUtils;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageUtils;
import com.ft.sdk.garble.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * BY huangDianHua
 * DATE:2019-12-02 16:43
 * Description
 */
public class FTAutoTrack {
    public final static String TAG = Constants.LOG_TAG_PREFIX + "AutoTrack";


    /**
     * Start APP
     * Warning!!! This method cannot be deleted
     * <p>
     * This method was originally called by the FT Plugin
     */
    public static void startApp(Application app) {
        try {
            LogUtils.d(TAG, "startApp:" + app);
            //Determine if it is the main process
            if (Utils.isMainProcess()) {
                FTActivityLifecycleCallbacks life = new FTActivityLifecycleCallbacks();

                if (app != null) {
                    Class<?> clazz = PackageUtils.getSophixClass();
                    if (clazz == null || !clazz.isInstance(app)) {
                        app.registerActivityLifecycleCallbacks(life);
                    }
                } else {
                    getApplication().registerActivityLifecycleCallbacks(life);
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
    }

    /**
     * Start APP
     * Warning!!! This method cannot be deleted
     * <p>
     * This method was originally called by the FT Plugin
     * Compatible with versions before ft-plugin:1.2.0-beta03
     */
    public static void startApp(Object object) {
        LogUtils.d(TAG, "invoke compatible start app");
        startApp(null);
    }

    /**
     * How Activity is opened (marks whether opened from Fragment or Activity)
     * Warning!!! This method cannot be deleted
     * This method was originally called by the FT Plugin
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
     * Activity started
     * Warning!!! This method cannot be deleted
     *
     * @deprecated This method was originally called by the FT Plugin, currently no longer used.
     */
    @Deprecated
    public static void activityOnCreate(Class clazz) {
        try {
            //startPage(clazz);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }


    /**
     * Activity closed
     * Warning!!! This method cannot be deleted
     *
     * @deprecated This method was originally called by the FT Plugin, currently no longer used.
     */
    @Deprecated
    public static void activityOnDestroy(Class clazz) {
        try {
            //destroyPage(clazz);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }

    /**
     * Notify Fragment's display and hide status
     * Warning!!! This method cannot be deleted
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
     * Fragment opened
     * Warning!!! This method cannot be deleted
     *
     * @param clazz
     * @param activity
     * @deprecated This method was originally called by the FT Plugin, currently no longer used.
     */
    @Deprecated
    public static void fragmentOnResume(Object clazz, Object activity) {
        try {
            //LogUtils.d("Fragment[\nOnCreateView=====>fragment:"+((Class)clazz).getSimpleName());
            //startPage(clazz, activity);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }

    /**
     * Fragment closed
     * Warning!!! This method cannot be deleted
     *
     * @param clazz
     * @param activity
     * @deprecated This method was originally called by the FT Plugin, currently no longer used.
     */
    @Deprecated
    public static void fragmentOnPause(Object clazz, Object activity) {
        try {
            //LogUtils.d("Fragment[\nOnDestroyView=====>fragment:"+((Class)clazz).getSimpleName());
            //destroyPage(clazz, activity);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }

    /**
     * Click event
     * Warning!!! This method cannot be deleted
     *
     * @param view Clicked page {@link View}
     */
    public static void trackViewOnClick(View view) {
        if (view == null) {
            return;
        }
        trackViewOnClick(null, view, view.isPressed());
    }

    /**
     * RadioGroup click event
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
     * listView click event
     *
     * @param parent
     * @param v
     * @param position
     */
    public static void trackListView(AdapterView<?> parent, View v, int position) {
        trackViewOnClick(null, v, true);
    }

    /**
     * ExpandableList parent click event
     *
     * @param parent
     * @param v
     * @param position
     */
    public static void trackExpandableListViewOnGroupClick(ExpandableListView parent, View v, int position) {
        trackViewOnClick(null, v, true);
    }

    /**
     * TabHost switch
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
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }

    /**
     * Page switch of ViewPager
     *
     * @param object
     * @param position
     */
    public static void trackViewPagerChange(Object object, int position) {

    }

    /**
     * ExpandableList child click event
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
     * Click event
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
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }

    /**
     * Click event
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
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
    }

    /**
     * {@link MenuItem} click event
     *
     * @param menuItem
     */
    public static void trackMenuItem(MenuItem menuItem) {
        try {
            trackMenuItem(null, menuItem);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }

    /**
     * {@link MenuItem} click event
     *
     * @param object
     * @param menuItem
     */
    public static void trackMenuItem(Object object, MenuItem menuItem) {
        try {
            clickView((Class<?>) object, AopUtils.getClassName(object), AopUtils.getSupperClassName(object),
                    AopUtils.getMenuItem(menuItem));
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }

    /**
     * Dialog event listener
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
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }


    /**
     * Listen for touch events
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
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }

    /**
     * Click event
     *
     * @param clazz
     * @param currentPage
     * @param rootPage
     * @param vtp
     */
    public static void clickView(Class<?> clazz, String currentPage, String rootPage, String vtp) {
        LogUtils.showAlias("Current click event vtp value is:" + vtp);
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
     * Click event
     *
     * @param view
     * @param clazz
     * @param currentPage
     * @param rootPage
     * @param vtp
     */
    public static void clickView(View view, Class<?> clazz, String currentPage, String rootPage, String vtp) {
        LogUtils.showAlias("Current click event vtp value is:" + vtp);
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
     * Record click event
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
     * Record application login effectiveness
     */
    public static void putRUMLaunchPerformance(boolean isCold, long duration, long startTime) {
        FTRUMInnerManager.get().addAction(
                isCold ? Constants.ACTION_NAME_LAUNCH_COLD : Constants.ACTION_NAME_LAUNCH_HOT,
                isCold ? Constants.ACTION_TYPE_LAUNCH_COLD : Constants.ACTION_TYPE_LAUNCH_HOT,
                duration, startTime);
    }


    /**
     * Get corresponding method (Activity) execution time (This method will be called in the onCreate method of all activities that inherit AppCompatActivity)
     *
     * @param desc className + "|" + methodName + "|" + methodDesc
     * @param cost
     */
    public static void timingMethod(String desc, long cost) {
//        LogUtils.d(TAG, desc);
//        try {
//            String[] arr = desc.split("\\|");
//            String[] names = arr[0].split("/");
//            String pageName = names[names.length - 1];
//            handleOp(pageName, OP.OPEN, cost * 1000, null);
//        } catch (Exception e) {
//            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
//
//        }
    }

    /**
     * Plug-in method used to replace the called {@link android.webkit.WebView#loadUrl(String)} method. This method structure should be modified with caution, please synchronize the modification after the modification
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] The part of the visitMethodInsn method about this replacement
     *
     * @param webView
     * @param url
     * @return
     */

    public static void loadUrl(View webView, String url) {
        if (webView == null) {
            LogUtils.e(TAG, "WebView has not initialized.");
            return;
        }
        setUpWebView(webView);
        invokeWebViewLoad(webView, "loadUrl", new Object[]{url}, new Class[]{String.class});
    }

    /**
     * Plug-in method used to replace the called {@link android.webkit.WebView#loadUrl(String)} method. This method structure should be modified with caution, please synchronize the modification after the modification
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] The part of the visitMethodInsn method about this replacement
     *
     * @param webView
     * @param url
     * @param additionalHttpHeaders
     * @return
     */
    public static void loadUrl(View webView, String url, Map<String, String> additionalHttpHeaders) {
        if (webView == null) {
            LogUtils.e(TAG, "WebView has not initialized.");
            return;
        }
        setUpWebView(webView);
        invokeWebViewLoad(webView, "loadUrl", new Object[]{url, additionalHttpHeaders}, new Class[]{String.class, Map.class});
    }

    /**
     * Plug-in method used to replace the called {@link android.webkit.WebView#loadData(String, String, String)} method. This method structure should be modified with caution, please synchronize the modification after the modification
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] The part of the visitMethodInsn method about this replacement
     *
     * @param webView
     * @param data
     * @param encoding
     * @param mimeType
     * @return
     */
    public static void loadData(View webView, String data, String mimeType, String encoding) {
        if (webView == null) {
            LogUtils.e(TAG, "WebView has not initialized.");
            return;
        }
        setUpWebView(webView);
        invokeWebViewLoad(webView, "loadData", new Object[]{data, mimeType, encoding}, new Class[]{String.class, String.class, String.class});
    }

    /**
     * Plug-in method used to replace the called {@link android.webkit.WebView#loadDataWithBaseURL(String, String, String, String, String)} method. This method structure should be modified with caution, please synchronize the modification after the modification
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] The part of the visitMethodInsn method about this replacement
     *
     * @param webView
     * @param data
     * @param encoding
     * @param mimeType
     * @return
     */
    public static void loadDataWithBaseURL(View webView, String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        if (webView == null) {
            LogUtils.e(TAG, "WebView has not initialized.");
            return;
        }
        setUpWebView(webView);
        invokeWebViewLoad(webView, "loadDataWithBaseURL", new Object[]{baseUrl, data, mimeType, encoding, historyUrl},
                new Class[]{String.class, String.class, String.class, String.class, String.class});
    }

    /**
     * Plug-in method used to replace the called {@link android.webkit.WebView#postUrl(String, byte[])} method. This method structure should be modified with caution, please synchronize the modification after the modification
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] The part of the visitMethodInsn method about this replacement
     *
     * @param webView
     * @param url
     * @return
     */
    public static void postUrl(View webView, String url, byte[] postData) {
        if (webView == null) {
            LogUtils.e(TAG, "WebView has not initialized.");
            return;
        }
        setUpWebView(webView);
        invokeWebViewLoad(webView, "postUrl", new Object[]{url, postData},
                new Class[]{String.class, byte[].class});
    }

    /**
     * Set webview listener event
     *
     * @param webView
     */
    public static void setUpWebView(View webView) {
        if (webView instanceof WebView && webView.getTag(R.id.ft_webview_handled_tag_view_value) == null) {
            new FTWebViewHandler().setWebView((WebView) webView);
        }
    }

    /**
     * Call webview method
     *
     * @param webView
     * @param methodName Method name
     * @param params     Parameter
     * @param paramTypes
     */
    private static void invokeWebViewLoad(View webView, String methodName, Object[] params, Class[] paramTypes) {
        try {
            Class<?> clazz = webView.getClass();
            Method loadMethod = clazz.getMethod(methodName, paramTypes);
            loadMethod.invoke(webView, params);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }

    /**
     * Plug-in method used to replace the called {@link OkHttpClient.Builder#build() } method. This method structure should be modified with caution, please synchronize the modification after the modification
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] The part of the visitMethodInsn method about this replacement
     *
     * @param builder
     * @return
     */
    public static OkHttpClient trackOkHttpBuilder(OkHttpClient.Builder builder) {
        if (FTSdk.checkInstallState()) {
            LogUtils.d(TAG, "trackOkHttpBuilder");
        } else {
            LogUtils.e(TAG, "trackOkHttpBuilder: OkhttpClient.Build Before SDK install");
        }
        //Found compatibility issues in some projects
        if (FTRUMConfigManager.get().isRumEnable()) {
            FTRUMConfig config = FTRUMConfigManager.get().getConfig();
            //config nonnull here ignore warning
            if (config.isEnableTraceUserResource()) {
                boolean hasSetResource = false;//Whether FTResourceInterceptor has been set
                for (Interceptor interceptor : builder.interceptors()) {
                    if (interceptor instanceof FTResourceInterceptor) {
                        hasSetResource = true;
                        break;
                    }

                }
                if (!hasSetResource) {
                    FTResourceInterceptor.ContentHandlerHelper contentHandler = FTRUMConfigManager.get()
                            .getOverrideResourceContentHandler();
                    if (contentHandler != null) {
                        builder.interceptors().add(0, new FTResourceInterceptor(contentHandler));
                    } else {
                        builder.interceptors().add(0, new FTResourceInterceptor());
                    }
                } else {
                    LogUtils.d(TAG, "Skip default FTResourceInterceptor setting");
                }

                OkHttpClient client = builder.build();
                EventListener.Factory originFactory = client.eventListenerFactory();
                if (originFactory instanceof FTResourceEventListener.FTFactory) {
                    LogUtils.d(TAG, "Skip default FTResourceEventListener.FTFactory setting");
                } else {
                    FTResourceEventListener.FTFactory overrideFactory = FTRUMConfigManager.get()
                            .getOverrideEventListener();
                    if (overrideFactory != null) {
                        builder.eventListenerFactory(overrideFactory);
                    } else {
                        builder.eventListenerFactory(new FTResourceEventListener
                                .FTFactory(config.isEnableResourceHostIP(), config.getResourceUrlHandler(), originFactory));
                    }
                }
            }
        }

        if (FTTraceConfigManager.get().isEnableAutoTrace()) {
            boolean hasSetTrace = false;//Whether FTResourceInterceptor has been set

            for (Interceptor interceptor : builder.interceptors()) {
                if (interceptor instanceof FTTraceInterceptor) {
                    hasSetTrace = true;
                    break;
                }

            }
            if (!hasSetTrace) {
                FTTraceInterceptor.HeaderHandler headerHandler = FTTraceConfigManager.get().getOverrideHeaderHandler();
                if (headerHandler != null) {
                    builder.interceptors().add(0, new FTTraceInterceptor(headerHandler));
                } else {
                    builder.interceptors().add(0, new FTTraceInterceptor());
                }
            } else {
                LogUtils.d(TAG, "Skip default FTTraceInterceptor setting");
            }
        }
        return builder.build();
    }

    /**
     * Plug-in method used to replace the called {@link Request.Builder#build() } method. This method structure should be modified with caution, please synchronize the modification after the modification
     * [com.ft.plugin.garble.bytecode.FTMethodAdapter] The part of the visitMethodInsn method about this replacement
     *
     * @param builder
     * @return
     */
    public static Request trackRequestBuilder(Request.Builder builder) {
        if (FTSdk.checkInstallState()) {
            LogUtils.d(TAG, "trackRequestBuilder");
        } else {
            LogUtils.e(TAG, "trackRequestBuilder: Request.Builder Before SDK install");
        }
        if (FTRUMConfigManager.get().isRumEnable()) {
            FTSDKConfig config = FTSdk.get().getBaseConfig();
            if (config.isEnableOkhttpRequestTag()) {
                ResourceID uuid = builder.build().tag(ResourceID.class);
                if (uuid == null) {
                    builder.tag(ResourceID.class, new ResourceID());
                }
            }
        }
        return builder.build();
    }


}
