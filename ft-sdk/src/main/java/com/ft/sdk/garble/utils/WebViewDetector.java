package com.ft.sdk.garble.utils;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * WebView detection utility class
 * Used to check if Activity or Fragment's contentView contains WebView
 *
 * @author Brandon
 */
public class WebViewDetector {

    private static final String LOG_TAG = Constants.LOG_TAG_PREFIX + "WebViewDetector";

    /**
     * Check if Activity's contentView contains WebView
     *
     * @param activity Activity instance
     * @return true if contains WebView, otherwise returns false
     */
    public static boolean hasWebView(Activity activity) {
        if (activity == null) {
            LogUtils.w(LOG_TAG, "Activity is null");
            return false;
        }

        try {
            View contentView = activity.findViewById(android.R.id.content);
            if (contentView != null) {
                return findWebViewInViewTree(contentView);
            }
        } catch (Exception e) {
            LogUtils.e(LOG_TAG, "Error checking WebView in Activity: " + e.getMessage());
        }

        return false;
    }

    /**
     * Check if AndroidX Fragment's view contains WebView
     *
     * @param fragment AndroidX Fragment instance
     * @return true if contains WebView, otherwise returns false
     */
    public static boolean hasWebView(androidx.fragment.app.Fragment fragment) {
        if (fragment == null) {
            LogUtils.w(LOG_TAG, "Fragment is null");
            return false;
        }

        try {
            View view = fragment.getView();
            if (view != null) {
                return findWebViewInViewTree(view);
            }
        } catch (Exception e) {
            LogUtils.e(LOG_TAG, "Error checking WebView in AndroidX Fragment: " + e.getMessage());
        }

        return false;
    }

    /**
     * Check if Android app Fragment's view contains WebView
     *
     * @param fragment Android app Fragment instance
     * @return true if contains WebView, otherwise returns false
     */
    public static boolean hasWebView(Fragment fragment) {
        if (fragment == null) {
            LogUtils.w(LOG_TAG, "Fragment is null");
            return false;
        }

        try {
            View view = fragment.getView();
            if (view != null) {
                return findWebViewInViewTree(view);
            }
        } catch (Exception e) {
            LogUtils.e(LOG_TAG, "Error checking WebView in Android app Fragment: " + e.getMessage());
        }

        return false;
    }

    /**
     * Check if the specified View and its child Views contain WebView
     *
     * @param view View to check
     * @return true if contains WebView, otherwise returns false
     */
    public static boolean hasWebView(View view) {
        if (view == null) {
            return false;
        }
        return findWebViewInViewTree(view);
    }

    /**
     * Recursively traverse View tree to find WebView
     *
     * @param view Root View
     * @return true if WebView is found, otherwise returns false
     */
    private static boolean findWebViewInViewTree(View view) {
        if (view == null) {
            return false;
        }

        // Check if current View is WebView
        if (isWebView(view)) {
            return true;
        }

        // If it's a ViewGroup, recursively check child Views
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = viewGroup.getChildAt(i);
                if (findWebViewInViewTree(child)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if the specified View is a WebView (supports standard WebView, TBS WebView and DC WebView)
     *
     * @param view View to check
     * @return true if it's a WebView, otherwise returns false
     */
    private static boolean isWebView(View view) {
        if (view == null) {
            return false;
        }

        // Check standard WebView
        if (view instanceof WebView) {
            return true;
        }

        // Check TBS WebView
        if (TBSWebViewUtils.isTBSWebViewInstance(view)) {
            return true;
        }

        // Check DC WebView
        if (DCSWebViewUtils.isDCWebViewInstance(view)) {
            return true;
        }

        return false;
    }

    /**
     * Find the first WebView instance from View tree
     *
     * @param view Root View
     * @return The first WebView found, or null if not found
     */
    public static View findFirstWebView(View view) {
        if (view == null) {
            return null;
        }

        if (isWebView(view)) {
            return view;
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = viewGroup.getChildAt(i);
                View webView = findFirstWebView(child);
                if (webView != null) {
                    return webView;
                }
            }
        }

        return null;
    }

    /**
     * Find the first WebView instance from Activity's contentView
     *
     * @param activity Activity instance
     * @return The first WebView found, or null if not found
     */
    public static View findFirstWebView(Activity activity) {
        if (activity == null) {
            return null;
        }

        try {
            View contentView = activity.findViewById(android.R.id.content);
            if (contentView != null) {
                return findFirstWebView(contentView);
            }
        } catch (Exception e) {
            LogUtils.e(LOG_TAG, "Error finding WebView in Activity: " + e.getMessage());
        }

        return null;
    }

    /**
     * Find the first WebView instance from AndroidX Fragment's view
     *
     * @param fragment AndroidX Fragment instance
     * @return The first WebView found, or null if not found
     */
    public static View findFirstWebView(androidx.fragment.app.Fragment fragment) {
        if (fragment == null) {
            return null;
        }

        try {
            View view = fragment.getView();
            if (view != null) {
                return findFirstWebView(view);
            }
        } catch (Exception e) {
            LogUtils.e(LOG_TAG, "Error finding WebView in AndroidX Fragment: " + e.getMessage());
        }

        return null;
    }

    /**
     * Find the first WebView instance from Android app Fragment's view
     *
     * @param fragment Android app Fragment instance
     * @return The first WebView found, or null if not found
     */
    public static View findFirstWebView(Fragment fragment) {
        if (fragment == null) {
            return null;
        }

        try {
            View view = fragment.getView();
            if (view != null) {
                return findFirstWebView(view);
            }
        } catch (Exception e) {
            LogUtils.e(LOG_TAG, "Error finding WebView in Android app Fragment: " + e.getMessage());
        }

        return null;
    }
}

