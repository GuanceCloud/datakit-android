package com.ft.sdk;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ft.sdk.garble.utils.Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class FTViewPermanentIdResolver {
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    private FTViewPermanentIdResolver() {
    }

    static String resolve(View view) {
        String path = resolvePath(view);
        if (Utils.isNullOrEmpty(path)) {
            return null;
        }
        return md5(path);
    }

    static String resolvePath(View view) {
        if (view == null) {
            return null;
        }

        StringBuilder path = new StringBuilder();
        path.append(resolvePackageName(view));
        path.append("/");
        path.append(escape(resolveScreenNamespace(view)));

        StringBuilder viewPath = new StringBuilder();
        View current = view;
        while (current != null) {
            viewPath.insert(0, "/" + resolveViewSegment(current));
            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }
        path.append(viewPath);
        return path.toString();
    }

    private static String resolvePackageName(View view) {
        Context context = view.getContext();
        if (context != null && !Utils.isNullOrEmpty(context.getPackageName())) {
            return escape(context.getPackageName());
        }

        Application application = FTApplication.getApplication();
        if (application != null && !Utils.isNullOrEmpty(application.getPackageName())) {
            return escape(application.getPackageName());
        }
        return "unknown";
    }

    private static String resolveScreenNamespace(View view) {
        String viewName = FTRUMInnerManager.get().getViewName();
        if (!Utils.isNullOrEmpty(viewName)) {
            return viewName;
        }

        Context context = view.getContext();
        if (context != null) {
            String contextName = context.getClass().getName();
            if (!Utils.isNullOrEmpty(contextName)) {
                return contextName;
            }
        }

        View rootView = view.getRootView();
        if (rootView != null) {
            String rootResourceName = resolveResourceName(rootView);
            if (!Utils.isNullOrEmpty(rootResourceName)) {
                return rootResourceName;
            }
            return rootView.getClass().getName();
        }
        return view.getClass().getName();
    }

    private static String resolveViewSegment(View view) {
        String resourceName = resolveResourceName(view);
        if (!Utils.isNullOrEmpty(resourceName)) {
            return escape(resourceName);
        }

        String className = view.getClass().getName();
        return "cls:" + escape(className) + "#" + resolveSameClassSiblingIndex(view);
    }

    private static String resolveResourceName(View view) {
        if (view.getId() == View.NO_ID) {
            return null;
        }

        try {
            Resources resources = view.getResources();
            return resources == null ? null : resources.getResourceName(view.getId());
        } catch (Resources.NotFoundException ignored) {
            return null;
        }
    }

    private static int resolveSameClassSiblingIndex(View view) {
        ViewParent parent = view.getParent();
        if (!(parent instanceof ViewGroup)) {
            return 0;
        }

        ViewGroup parentGroup = (ViewGroup) parent;
        int index = 0;
        Class<?> viewClass = view.getClass();
        for (int i = 0; i < parentGroup.getChildCount(); i++) {
            View child = parentGroup.getChildAt(i);
            if (child == view) {
                return index;
            }
            if (child != null && child.getClass() == viewClass) {
                index++;
            }
        }
        return 0;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("%", "%25").replace("/", "%2F");
    }

    private static String md5(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(value.getBytes());
            char[] hexChars = new char[digest.length * 2];
            for (int i = 0; i < digest.length; i++) {
                int v = digest[i] & 0xFF;
                hexChars[i * 2] = HEX_ARRAY[v >>> 4];
                hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
            }
            return new String(hexChars);
        } catch (NoSuchAlgorithmException ignored) {
            return null;
        }
    }
}
