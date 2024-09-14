package com.ft.sdk.sessionreplay.internal.recorder;

import android.os.Build;
import android.view.View;

import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WindowInspector {
    private static final String TAG = "WindowInspector";

    private WindowInspector() {
    }

    private static final String FAILED_TO_RETRIEVE_DECOR_VIEWS_ERROR_MESSAGE =
            "SR WindowInspector failed to retrieve the decor views";

    private static Class<?> GLOBAL_WM_CLASS;
    private static Object GLOBAL_WM_INSTANCE;
    private static Field VIEWS_FIELD;

    static {
        try {
            GLOBAL_WM_CLASS = Class.forName("android.view.WindowManagerGlobal");
            GLOBAL_WM_INSTANCE = GLOBAL_WM_CLASS.getMethod("getInstance").invoke(null);
            VIEWS_FIELD = GLOBAL_WM_CLASS.getDeclaredField("mViews");
            VIEWS_FIELD.setAccessible(true);
        } catch (Throwable e) {
            // Log or handle initialization errors
            e.printStackTrace();
        }
    }

    public static List<View> getGlobalWindowViews(InternalLogger internalLogger) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return android.view.inspector.WindowInspector.getGlobalWindowViews();
            } else {
                return getGlobalWindowViewsLegacy(GLOBAL_WM_INSTANCE, VIEWS_FIELD);
            }
        } catch (Throwable e) {
            internalLogger.e(TAG, FAILED_TO_RETRIEVE_DECOR_VIEWS_ERROR_MESSAGE, e,true);

            return List.of();
        }
    }

    private static List<View> getGlobalWindowViewsLegacy(Object globalWmInstance, Field viewsField) {
        if (globalWmInstance != null && viewsField != null) {
            try {
                Object views = viewsField.get(globalWmInstance);
                if (views instanceof List<?>) {
                    List<?> list = (List<?>) views;
                    List<View> decorViews = new ArrayList<>();
                    for (Object obj : list) {
                        if (obj instanceof View) {
                            decorViews.add((View) obj);
                        }
                    }
                    return decorViews;
                } else if (views instanceof Object[]) {
                    Object[] array = (Object[]) views;
                    List<View> decorViews = new ArrayList<>();
                    for (Object obj : array) {
                        if (obj instanceof View) {
                            decorViews.add((View) obj);
                        }
                    }
                    return decorViews;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return List.of();
    }
}
