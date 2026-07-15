package com.ft.sdk.sessionreplay.internal.recorder;

import android.view.View;

import java.lang.reflect.Method;

final class PermanentIdResolver {

    private PermanentIdResolver() {
    }

    static String resolve(View view) {
        if (view == null) {
            return null;
        }

        try {
            Class<?> managerClass = Class.forName("com.ft.sdk.SessionReplayManager");
            Object manager = managerClass.getMethod("get").invoke(null);
            if (manager == null) {
                return null;
            }
            Method method = managerClass.getMethod("resolvePermanentId", View.class);
            Object value = method.invoke(manager, view);
            return value instanceof String ? (String) value : null;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
