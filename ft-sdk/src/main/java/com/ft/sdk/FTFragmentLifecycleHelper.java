package com.ft.sdk;

import android.app.Activity;
import android.os.Build;

import com.ft.sdk.garble.utils.PackageUtils;

/**
 * Listener
 */
public class FTFragmentLifecycleHelper {
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

    /**
     * Add listener for Fragment view activity
     * @param activity
     */
    public void register(Activity activity) {
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        //config nonnull here ignore warning
        if (manager.isRumEnable() && manager.getConfig().isEnableTraceUserViewInFragment()) {
            if (androidXFragmentLifecycleCallbacks != null
                    && activity instanceof androidx.fragment.app.FragmentActivity) {
                ((androidx.fragment.app.FragmentActivity) activity).getSupportFragmentManager()
                        .registerFragmentLifecycleCallbacks(androidXFragmentLifecycleCallbacks, true);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.getFragmentManager()
                            .registerFragmentLifecycleCallbacks(oreoFragmentLifecycleCallbacks, true);
                }
            }
        }

    }

    /**
     * Remove listener for Fragment view activity
     * @param activity
     */
    public void unregister(Activity activity) {
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        //config nonnull here ignore warning
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
