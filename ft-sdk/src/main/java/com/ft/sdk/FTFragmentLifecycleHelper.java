package com.ft.sdk;

import android.app.Activity;
import android.os.Build;
import android.os.SystemClock;

import com.ft.sdk.garble.utils.PackageUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment lifecycle bridge that unifies AndroidX and platform fragments and
 * forwards lifecycle events to RUM view tracking.
 * <p>
 * Responsibilities:
 * - Registers appropriate {@code FragmentManager.FragmentLifecycleCallbacks}
 *   depending on runtime environment (AndroidX vs platform and API level).
 * - Measures time from {@code onFragmentPreAttached} to {@code onFragmentCreated}
 *   and reports view creation duration to {@link FTRUMInnerManager}.
 * - Starts and stops RUM views when fragments resume/stop. If a
 *   {@link FTViewFragmentTrackingHandler} is configured, uses its mapping of
 *   {@link FragmentWrapper} to a {@link HandlerView} for custom view naming and
 *   attributes; otherwise falls back to the fragment simple class name.
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
                HandlerView view = viewHandler.resolveHandlerView(wrapper);
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
            HandlerView view = viewHandler.resolveHandlerView(wrapper);
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
            HandlerView view = viewHandler.resolveHandlerView(wrapper);
            if (view != null) {
                FTRUMInnerManager.get().stopView();
            }
        } else {
            FTRUMInnerManager.get().stopView();
        }
    }


    /**
     * Register fragment lifecycle callbacks on the given {@link Activity} so
     * that fragment RUM view tracking is enabled for this activity.
     * <p>
     * This is a no-op unless RUM is enabled and fragment view tracing is
     * enabled in {@link FTRUMConfigManager}.
     *
     * @param activity host activity that contains fragments.
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
     * Unregister previously registered fragment lifecycle callbacks for the
     * given {@link Activity}.
     *
     * @param activity host activity that contains fragments.
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
