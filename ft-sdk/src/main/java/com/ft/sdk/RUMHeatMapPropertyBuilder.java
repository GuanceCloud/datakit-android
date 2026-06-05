package com.ft.sdk;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ft.sdk.garble.utils.AopUtils;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;
import java.util.LinkedHashMap;

final class RUMHeatMapPropertyBuilder {
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_SELECTOR = "selector";
    private static final String KEY_VIEWPORT = "viewport";

    private RUMHeatMapPropertyBuilder() {
    }

    static HashMap<String, Object> buildActionProperties(View targetView, MotionEvent motionEvent) {
        HashMap<String, Object> properties = new HashMap<>();
        appendActionTarget(properties, targetView);
        appendActionPosition(properties, targetView, motionEvent);
        appendDisplay(properties, targetView);
        return properties;
    }

    static void appendDisplay(HashMap<String, Object> properties) {
        appendDisplay(properties, null);
    }

    static void appendDisplay(HashMap<String, Object> properties, View view) {
        if (properties == null || properties.containsKey(Constants.KEY_RUM_DISPLAY)) {
            return;
        }
        Viewport viewport = resolveViewport(view);
        if (viewport != null) {
            properties.put(Constants.KEY_RUM_DISPLAY,
                    buildDisplayJson(viewport.width, viewport.height));
        }
    }

    static String buildActionPositionJson(float x, float y) {
        HashMap<String, Object> position = new LinkedHashMap<>();
        position.put(KEY_X, x);
        position.put(KEY_Y, y);
        return Utils.hashMapObjectToJson(position);
    }

    static String buildActionTargetJson(int width, int height, String selector) {
        HashMap<String, Object> target = new LinkedHashMap<>();
        target.put(KEY_WIDTH, width);
        target.put(KEY_HEIGHT, height);
        target.put(KEY_SELECTOR, selector);
        return Utils.hashMapObjectToJson(target);
    }

    static String buildDisplayJson(int width, int height) {
        HashMap<String, Object> viewport = new LinkedHashMap<>();
        viewport.put(KEY_WIDTH, width);
        viewport.put(KEY_HEIGHT, height);

        HashMap<String, Object> display = new LinkedHashMap<>();
        display.put(KEY_VIEWPORT, viewport);
        return Utils.hashMapObjectToJson(display);
    }

    private static void appendActionTarget(HashMap<String, Object> properties, View targetView) {
        if (targetView == null) {
            return;
        }
        properties.put(Constants.KEY_RUM_ACTION_TARGET,
                buildActionTargetJson(targetView.getWidth(), targetView.getHeight(),
                        resolveSelector(targetView)));
    }

    private static void appendActionPosition(HashMap<String, Object> properties, View targetView,
                                             MotionEvent motionEvent) {
        if (targetView == null || motionEvent == null) {
            return;
        }

        int pointerIndex = motionEvent.getActionIndex();
        if (pointerIndex < 0 || pointerIndex >= motionEvent.getPointerCount()) {
            pointerIndex = 0;
        }
        properties.put(Constants.KEY_RUM_ACTION_POSITION,
                buildActionPositionJson(motionEvent.getX(pointerIndex), motionEvent.getY(pointerIndex)));
    }

    private static String resolveSelector(View targetView) {
        StringBuilder selector = new StringBuilder(resolveViewSegment(targetView));
        ViewParent parent = targetView.getParent();
        while (parent instanceof View) {
            View parentView = (View) parent;
            selector.insert(0, resolveViewSegment(parentView) + "/");
            parent = parentView.getParent();
        }
        Context context = targetView.getContext();
        if (context != null) {
            selector.insert(0, context.getClass().getSimpleName() + "/");
        }
        return selector.toString();
    }

    private static String resolveViewSegment(View view) {
        StringBuilder segment = new StringBuilder(view.getClass().getSimpleName());
        String viewId = AopUtils.getViewId(view);
        if (!Utils.isNullOrEmpty(viewId)) {
            segment.append("#").append(viewId);
        }
        segment.append("[").append(resolveChildIndex(view)).append("]");
        return segment.toString();
    }

    private static int resolveChildIndex(View view) {
        ViewParent parent = view.getParent();
        if (!(parent instanceof ViewGroup)) {
            return 0;
        }

        ViewGroup parentGroup = (ViewGroup) parent;
        for (int i = 0; i < parentGroup.getChildCount(); i++) {
            if (parentGroup.getChildAt(i) == view) {
                return i;
            }
        }
        return 0;
    }

    private static Viewport resolveViewport(View view) {
        Viewport viewport = resolveVisibleViewport(view);
        if (viewport != null) {
            return viewport;
        }

        Resources resources = resolveResources(view);
        if (resources == null) {
            return null;
        }

        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (displayMetrics.widthPixels <= 0 || displayMetrics.heightPixels <= 0) {
            return null;
        }
        return new Viewport(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    private static Viewport resolveVisibleViewport(View view) {
        if (view == null) {
            return null;
        }
        View rootView = view.getRootView();
        if (rootView == null) {
            return null;
        }

        Rect visibleFrame = new Rect();
        rootView.getWindowVisibleDisplayFrame(visibleFrame);
        if (visibleFrame.width() <= 0 || visibleFrame.height() <= 0) {
            return null;
        }
        return new Viewport(visibleFrame.width(), visibleFrame.height());
    }

    private static Resources resolveResources(View view) {
        if (view != null) {
            Context context = view.getContext();
            if (context != null) {
                return context.getResources();
            }
        }

        Application application = FTApplication.getApplication();
        return application == null ? null : application.getResources();
    }

    private static class Viewport {
        final int width;
        final int height;

        Viewport(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
