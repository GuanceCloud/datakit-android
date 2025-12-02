package com.ft.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.garble.gesture.WindowCallbackTracker;
import com.ft.sdk.garble.manager.SlotIdWebviewBinder;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.WebViewDetector;

import java.util.HashMap;


/**
 * BY huangDianHua
 * DATE:2019-12-06 11:18
 * Description: {@link Activity} lifecycle callback class
 * <p>
 * Used to listen to {@link Activity} lifecycle, combined with {@link LifeCircleTraceCallback}, in
 * {@link #onActivityPreStarted(Activity)},{@link #onActivityPreCreated(Activity, Bundle)}, {@link #onActivityPostCreated(Activity, Bundle)}
 * to output {@link Activity} as {@link  Constants#FT_MEASUREMENT_RUM_VIEW} metric
 * Page load time: {@link Constants#KEY_RUM_VIEW_LOAD}
 * Startup time: {@link Constants#ACTION_TYPE_LAUNCH_HOT},{@link Constants#ACTION_NAME_LAUNCH_COLD}
 * <p>
 * These can be viewed through Guanceyun Studio <a href="https://docs.guance.com/real-user-monitoring/explorer/view/">Viewer View</a>
 */
public class FTActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTActivityLifecycleCallbacks";
    private final LifeCircleTraceCallback mAppRestartCallback = new LifeCircleTraceCallback();
    private final WindowCallbackTracker mDispatcherReceiver = new WindowCallbackTracker();

    private static int activityCount = 0;

    private final FTFragmentLifecycleHelper mFragmentLifecycleHelper = new FTFragmentLifecycleHelper();

    /**
     * {@link Activity} created
     * <p>
     * no use
     *
     * @param activity           {@link Activity}.
     * @param savedInstanceState {@link Activity#onSaveInstanceState(Bundle)}
     */
    @Override
    public void onActivityCreated(@NonNull Activity activity, @NonNull Bundle savedInstanceState) {
    }

    /**
     * Called before {@link Activity#onStart()} , handled by {@link LifeCircleTraceCallback#onPreStart()},
     * logic in integrated app {@link Activity#onStart()}
     *
     * @param activity {@link Activity}.
     */
    @Override
    public void onActivityPreStarted(@NonNull Activity activity) {
        mAppRestartCallback.onPreStart();

    }

    /**
     * Called after {@link Activity#onStart()}, no use
     *
     * @param activity {@link Activity}.
     */
    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        activityCount++;
        mFragmentLifecycleHelper.register(activity);
        if (activityCount > 0) {
            FTActivityManager.get().appForeground();
        }
    }

    /**
     * {@link LifeCircleTraceCallback#onPreOnCreate(Context)} used to record app start time
     *
     * @param activity           {@link Activity}.
     * @param savedInstanceState {@link Activity#onSaveInstanceState(Bundle)}.
     */
    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        mAppRestartCallback.onPreOnCreate(activity);
    }

    /**
     * {@link LifeCircleTraceCallback#onPostOnCreate(Context)} used to record app creation completion time
     *
     * @param activity           {@link Activity }.
     * @param savedInstanceState {@link Activity#onSaveInstanceState(Bundle)}.
     */
    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        mAppRestartCallback.onPostOnCreate(activity);

    }

    @Override
    public void onActivityPostStarted(@NonNull Activity activity) {
        mAppRestartCallback.onPostOnStart(activity);
    }

    /**
     * {@link Activity#onResume()} resume event
     * <p>
     * When {@link FTRUMConfigManager#isRumEnable()} is enabled, use {@link FTRUMGlobalManager#startView(String, HashMap)}
     *
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityResumed(@NonNull Activity activity) {

        //page open data insertion
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        if (manager.isRumEnable() && manager.getConfig().isEnableTraceUserView()) {
            FTViewActivityTrackingHandler handler = manager.getConfig().getViewActivityTrackingHandler();
            if (handler != null) {
                HandlerView view = handler.resolveHandlerView(activity);
                if (view != null) {
                    FTRUMInnerManager.get().startView(view.getViewName(), view.getProperty());
                }
            } else {
                FTRUMInnerManager.get().startView(activity.getClass().getSimpleName());
            }

            if (FTSdk.isSessionReplaySupport() && SessionReplayManager.get().hasRumLinkKeys()) {
                View view = WebViewDetector.findFirstWebView(activity);
                if (view != null) {
                    String viewId = FTRUMInnerManager.get().getViewId();
                    long slotId = System.identityHashCode(view);
                    LogUtils.d(TAG, "Track SlotID,activity map viewId:" + viewId + ",slotId:" + slotId);
                    SlotIdWebviewBinder.get().bind(slotId, viewId);
                }
            }


        }

        if (manager.isRumEnable() && manager.getConfig().isEnableTraceUserAction()) {
            mDispatcherReceiver.startTrack(activity.getWindow());
        }

        //start sync
        if (FTSdk.checkInstallState()) {
            SyncTaskManager.get().executeSyncPoll();
        }


    }

    /**
     * Called after {@link Activity#onPostResume()}, {@link  LifeCircleTraceCallback#onPostResume(Context)}
     *
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {
        mAppRestartCallback.onPostResume(activity);
    }

    /**
     * Called when {@link Activity#onPause()}, when {@link FTRUMConfigManager#isRumEnable()} is enabled,
     * use {@link FTRUMInnerManager#stopView()}
     *
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        //page close data insertion
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        if (manager.isRumEnable() && manager.getConfig().isEnableTraceUserView()) {
            FTViewActivityTrackingHandler handler = manager.getConfig().getViewActivityTrackingHandler();
            if (handler != null) {
                HandlerView view = handler.resolveHandlerView(activity);
                if (view != null) {
                    FTRUMInnerManager.get().stopView();
                }
            } else {
                FTRUMInnerManager.get().stopView();
            }
        }

        if (manager.isRumEnable() && manager.getConfig().isEnableTraceUserAction()) {
            mDispatcherReceiver.stopTrack(activity.getWindow());
        }

    }

    /**
     * Called when {@link Activity#onStop()}, {@link  LifeCircleTraceCallback#onEnterBackground()} handles page sleep processing
     *
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        activityCount--;
        if (activityCount == 0) {
            mAppRestartCallback.onEnterBackground();
            FTActivityManager.get().appBackGround();
        }
        mFragmentLifecycleHelper.unregister(activity);
    }


    /**
     * {@link Activity} save state
     * <p>
     * no use
     *
     * @param activity {@link Activity }.
     * @param outState {@link Activity#onSaveInstanceState(Bundle)}.
     */
    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    /**
     * {@link Activity} destroyed
     * <p>
     * no use
     *
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    /**
     * Called after {@link Activity#onDestroy()}, with {@link LifeCircleTraceCallback#onPostDestroy(Context)}
     * to handle some logic after {@link Activity} lifecycle ends
     *
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityPostDestroyed(Activity activity) {
        mAppRestartCallback.onPostDestroy(activity);

    }

    public static boolean isAppInForeground() {
        return activityCount > 0;
    }
}
