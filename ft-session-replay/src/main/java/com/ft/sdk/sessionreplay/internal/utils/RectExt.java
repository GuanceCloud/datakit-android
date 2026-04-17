package com.ft.sdk.sessionreplay.internal.utils;

import android.graphics.Rect;

import com.ft.sdk.sessionreplay.model.WireframeClip;

public class RectExt {
    public static WireframeClip toWireframeClip(Rect rect) {
        return new WireframeClip(
                (long) rect.top,
                (long) rect.bottom,
                (long) rect.left,
                (long) rect.right
        );
    }
}
