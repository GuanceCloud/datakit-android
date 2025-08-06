package com.ft.sdk;

import android.app.Activity;
import android.os.Build;
import android.os.SystemClock;

import com.ft.sdk.garble.utils.PackageUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener
 */
public class FTFragmentLifecycleHelper implements FragmentLifecycleCallBack {
    private AndroidXFragmentLifecycleCallbacks androidXFragmentLifecycleCallbacks;
    private OreoFragmentLifecycleCallbacks oreoFragmentLifecycleCallbacks;


    public FTFragmentLifecycleHelper() {
        if (PackageUtils.isAndroidXAvailable()) {
            androidXFragmentLifecycleCallbacks = new AndroidXFragmentLifecycleCallbacks();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            oreoFragmentLifecycleCallbacks = new OreoFragmentLifecycleCallbacks();
        }
    }

    private FTViewFragmentTrackingHandler viewHandler;

    private final Map<Object, Long> fragmentPreAttachedTimeMap = new HashMap<>();

    @Override
    public void onFragmentPreAttached(FragmentWrapper wrapper) {
        fragmentPreAttachedTimeMap.put(wrapper.getRealFragment(), SystemClock.elapsedRealtimeNanos());
    }

    @Override
    public void onFragmentCreated(FragmentWrapper wrapper) {
        Long preAttachedStartTime = fragmentPreAttachedTimeMap.remove(wrapper.getRealFragment());
        if (preAttachedStartTime != null) {
            // Calculate Fragment pre-attached to pre-created duration
            long createDuration = SystemClock.elapsedRealtimeNanos() - preAttachedStartTime;
            if (viewHandler != null) {
                HandlerView view = viewHandler.isInTake(wrapper);
                if (view != null) {
                    FTRUMInnerManager.get().onCreateView(view.getViewName(), createDuration);
                }
            } else {
                FTRUMInnerManager.get().onCreateView(wrapper.getSimpleClassName(), createDuration);
            }
        }
    }

    @Override
    public void onFragmentResumed(FragmentWrapper wrapper) {
        if (viewHandler != null) {
            HandlerView view = viewHandler.isInTake(wrapper);
            if (view != null) {
                FTRUMInnerManager.get().startView(view.getViewName(), view.getProperty());
            }
        } else {
            FTRUMInnerManager.get().startView(wrapper.getSimpleClassName());
        }
    }

    @Override
    public void onFragmentStopped(FragmentWrapper wrapper) {
        if (viewHandler != null) {
            HandlerView view = viewHandler.isInTake(wrapper);
            if (view != null) {
                FTRUMInnerManager.get().stopView();
            }
        } else {
            FTRUMInnerManager.get().stopView();
        }
    }


    /**
     * Add listener for Fragment view activity
     *
     * @param activity
     */
    public void register(Activity activity) {
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        if (manager.isRumEnable() && manager.getConfig().isEnableTraceUserViewInFragment()) {
            viewHandler = manager.getConfig().getViewFragmentTrackingHandler();
            if (androidXFragmentLifecycleCallbacks != null
                    && activity instanceof androidx.fragment.app.FragmentActivity) {
                androidXFragmentLifecycleCallbacks
                        .setFragmentLifecycleCallBack(this);
                ((androidx.fragment.app.FragmentActivity) activity).getSupportFragmentManager()
                        .registerFragmentLifecycleCallbacks(androidXFragmentLifecycleCallbacks, true);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    oreoFragmentLifecycleCallbacks
                            .setFragmentLifecycleCallBack(this);
                    activity.getFragmentManager()
                            .registerFragmentLifecycleCallbacks(oreoFragmentLifecycleCallbacks, true);
                }
            }
        }

    }

    /**
     * Remove listener for Fragment view activity
     *
     * @param activity
     */
    public void unregister(Activity activity) {
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        if (manager.isRumEnable() && manager.getConfig().isEnableTraceUserViewInFragment()) {
            if (androidXFragmentLifecycleCallbacks != null &&
                    activity instanceof androidx.fragment.app.FragmentActivity) {
                ((androidx.fragment.app.FragmentActivity) activity).getSupportFragmentManager()
                        .unregisterFragmentLifecycleCallbacks(androidXFragmentLifecycleCallbacks);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.getFragmentManager()
                            .unregisterFragmentLifecycleCallbacks(oreoFragmentLifecycleCallbacks);
                }
            }
        }
    }
}
