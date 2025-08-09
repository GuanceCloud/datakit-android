package com.ft.sdk;

import static com.ft.sdk.FTApplication.getApplication;

import android.app.Application;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;

import com.ft.sdk.garble.bean.ResourceID;
import com.ft.sdk.garble.utils.AopUtils;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageUtils;
import com.ft.sdk.garble.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
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
        trackViewOnClick(view, null, view.isPressed());
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
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("checkedId", checkedId);

        try {
            clickView(group, ActionSourceType.CLICK_RADIO_BUTTON, extra);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
    }

    /**
     * listView click event
     *
     * @param parent
     * @param v
     * @param position
     */
    public static void trackListView(AdapterView<?> parent, View v, int position) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("position", position);
        extra.put("parent", parent);
        try {
            clickView(v, ActionSourceType.CLICK_LIST_ITEM, extra);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
    }

    /**
     * ExpandableList parent click event
     *
     * @param parent
     * @param v
     * @param position
     */
    public static void trackExpandableListViewOnGroupClick(ExpandableListView parent, View v, int position) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("groupPosition", position);
        extra.put("parent", parent);
        try {
            clickView(v, ActionSourceType.CLICK_EXPAND_GROUP_ITEM, extra);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
    }

    /**
     * TabHost switch
     *
     * @param tabId
     */
    public static void trackTabHost(String tabId) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("tabId", tabId);
        trackViewOnClick(null, extra, true);
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
            //AopUtils.getViewDesc(view) + "#pos:" + position
            HashMap<String, Object> extra = new HashMap<>();
            extra.put("position", getPosition);
            clickView(view, ActionSourceType.CLICK_TAB, extra);
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
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("parent", parent);
        extra.put("parentPosition", parentPosition);
        extra.put("childPosition", childPosition);
        try {
            clickView(v, ActionSourceType.CLICK_EXPAND_LIST_ITEM, extra);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
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

            trackViewOnClick(view, null, view.isPressed());
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }

    /**
     * Click event
     *
     * @param view
     * @param isFromUser
     */
    public static void trackViewOnClick(View view, HashMap<String, Object> extra, boolean isFromUser) {
        try {
            if (isFromUser) {
                clickView(view, ActionSourceType.CLICK_VIEW, extra);
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
            clickView(menuItem, ActionSourceType.CLICK_MENU_ITEM, null);
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
            HashMap<String, Object> extra = new HashMap<>();
            extra.put("position", whichButton);
            clickView(dialog, ActionSourceType.CLICK_DIALOG_BUTTON, extra);
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
            HashMap<String, Object> extra = new HashMap<>();
            extra.put("motionEvent", motionEvent);
            clickView(view, ActionSourceType.CLICK_VIEW, extra);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }

    /**
     * Click event
     *
     * @param clickSourceType
     */
    public static void clickView(Object object, ActionSourceType clickSourceType, HashMap<String, Object> extra) {

        FTRUMConfigManager manager = FTRUMConfigManager.get();
        if (!manager.isRumEnable()) {
            return;
        }
        if (!manager.getConfig().isEnableTraceUserAction()) {
            return;
        }

        FTActionTrackingHandler handler = manager.getConfig().getActionTrackingHandler();
        if (handler != null) {
            HandlerAction action = manager.getConfig().getActionTrackingHandler()
                    .resolveHandlerAction(new ActionEventWrapper(object, clickSourceType, extra));

            if (action != null) {
                FTRUMInnerManager.get().startAction(action.getActionName(), Constants.EVENT_NAME_CLICK,
                        action.getProperty());
            }

        } else {

            String vtp = "";
            if (object instanceof View) {
                if (clickSourceType.equals(ActionSourceType.CLICK_VIEW)) {
                    vtp = AopUtils.getViewDesc((View) object);
                } else if (clickSourceType.equals(ActionSourceType.CLICK_LIST_ITEM)) {
                    vtp = AopUtils.getViewDesc((View) object) + "#position:" + extra.get("position");
                } else if (clickSourceType.equals(ActionSourceType.CLICK_EXPAND_GROUP_ITEM)) {
                    vtp = AopUtils.getViewDesc((View) object) + "#groupPosition:" + extra.get("groupPosition");
                } else if (clickSourceType.equals(ActionSourceType.CLICK_EXPAND_LIST_ITEM)) {
                    vtp = AopUtils.getViewDesc((View) object) + "#parentPosition:" + extra.get("parentPosition")
                            + "#childPosition:" + extra.get("childPosition");
                } else if (clickSourceType.equals(ActionSourceType.CLICK_RADIO_BUTTON)) {
                    vtp = AopUtils.getViewDesc((View) object) + "#checkedId:" + extra.get("checkedId");
                } else if (clickSourceType.equals(ActionSourceType.CLICK_TAB)) {
                    vtp = AopUtils.getViewDesc((View) object) + "#position:" + extra.get("position");
                }
            } else if (object instanceof MenuItem) {
                vtp = AopUtils.getMenuItem((MenuItem) object);
            } else if (object instanceof Dialog) {
                vtp = AopUtils.getDialogClickView((Dialog) object, (int) extra.get("position"));
            }

            LogUtils.showAlias("clickView:" + vtp + ",extra:" + extra);
            FTRUMInnerManager.get().startAction(vtp, Constants.EVENT_NAME_CLICK);

        }
    }


    /**
     * Record application login effectiveness
     */
    public static void putRUMLaunchPerformance(boolean isCold, long duration, long startTime) {
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        FTActionTrackingHandler handler = manager.getConfig().getActionTrackingHandler();
        if (handler != null) {
            HandlerAction action = handler.resolveHandlerAction(new ActionEventWrapper(null, isCold ? ActionSourceType.LAUNCH_COLD
                    : ActionSourceType.LAUNCH_HOT, null));

            if (action != null) {
                FTRUMInnerManager.get().addAction(
                        action.getActionName(),
                        isCold ? Constants.ACTION_TYPE_LAUNCH_COLD : Constants.ACTION_TYPE_LAUNCH_HOT,
                        duration, startTime, action.getProperty());
            }
        } else {
            FTRUMInnerManager.get().addAction(
                    isCold ? Constants.ACTION_NAME_LAUNCH_COLD : Constants.ACTION_NAME_LAUNCH_HOT,
                    isCold ? Constants.ACTION_TYPE_LAUNCH_COLD : Constants.ACTION_TYPE_LAUNCH_HOT,
                    duration, startTime);
        }
    }


    /**
     * Get corresponding method (Activity) execution time
     * (This method will be called in the onCreate method of all activities that inherit AppCompatActivity)
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
     * Plug-in method used to replace the called {@link android.webkit.WebView#loadUrl(String)} method.
     * This method structure should be modified with caution, please synchronize the modification after the modification
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
     * Plug-in method used to replace the called {@link android.webkit.WebView#loadUrl(String)} method.
     * This method structure should be modified with caution, please synchronize the modification after the modification
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
     * Plug-in method used to replace the called {@link android.webkit.WebView#loadData(String, String, String)} method.
     * This method structure should be modified with caution, please synchronize the modification after the modification
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
     * Plug-in method used to replace the called {@link android.webkit.WebView#loadDataWithBaseURL(String, String, String, String, String)} method.
     * This method structure should be modified with caution, please synchronize the modification after the modification
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
     * Plug-in method used to replace the called {@link android.webkit.WebView#postUrl(String, byte[])} method.
     * This method structure should be modified with caution, please synchronize the modification after the modification
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
     * Plug-in method used to replace the called {@link OkHttpClient.Builder#build() } method.
     * This method structure should be modified with caution, please synchronize the modification after the modification
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
     * Plug-in method used to replace the called {@link Request.Builder#build() } method.
     * This method structure should be modified with caution, please synchronize the modification after the modification
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
