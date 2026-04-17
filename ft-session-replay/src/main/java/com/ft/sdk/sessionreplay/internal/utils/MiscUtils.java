package com.ft.sdk.sessionreplay.internal.utils;

import static com.ft.sdk.sessionreplay.ColorConstant.OPAQUE_ALPHA_VALUE;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.TypedValue;
import android.view.WindowManager;
import android.view.WindowMetrics;

import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.utils.DefaultColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;

public class MiscUtils {

    public Integer resolveThemeColor(Resources.Theme theme) {
        TypedValue a = new TypedValue();
        theme.resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            return a.data;
        } else {
            return null;
        }
    }

    public SystemInformation resolveSystemInformation(Context context) {
        float screenDensity = context.getResources().getDisplayMetrics().density;
        String themeColorAsHexString = resolveThemeColor(context.getTheme()) != null
                ? new DefaultColorStringFormatter().formatColorAndAlphaAsHexString(resolveThemeColor(context.getTheme()), OPAQUE_ALPHA_VALUE)
                : null;

        return new SystemInformation(
                resolveScreenBounds(context, screenDensity),
                context.getResources().getConfiguration().orientation,
                screenDensity,
                themeColorAsHexString
        );
    }

    private GlobalBounds resolveScreenBounds(Context context, float screenDensity) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return new GlobalBounds(0, 0, 0, 0);
        }

        long screenHeight;
        long screenWidth;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics currentWindowMetrics = windowManager.getCurrentWindowMetrics();
            Rect screenBounds = currentWindowMetrics.getBounds();
            screenHeight = (long) ((screenBounds.bottom - screenBounds.top) / screenDensity);
            screenWidth = (long) ((screenBounds.right - screenBounds.left) / screenDensity);
        } else {
            Point size = new Point();
            windowManager.getDefaultDisplay().getSize(size);
            screenHeight = (long) (size.y / screenDensity);
            screenWidth = (long) (size.x / screenDensity);
        }
        return new GlobalBounds(0, 0, screenWidth, screenHeight);
    }
}
