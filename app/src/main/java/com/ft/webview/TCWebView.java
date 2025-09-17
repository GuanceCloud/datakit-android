package com.ft.webview;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Map;

public class TCWebView extends TCBaseWebView {
    public TCWebView(Context context, boolean b) {
        super(context, b);
    }

    public TCWebView(Context context) {
        super(context);
    }

    public TCWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public TCWebView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public TCWebView(Context context, AttributeSet attributeSet, int i, boolean b) {
        super(context, attributeSet, i, b);
    }

    public TCWebView(Context context, AttributeSet attributeSet, int i, Map<String, Object> map, boolean b) {
        super(context, attributeSet, i, map, b);
    }

}
