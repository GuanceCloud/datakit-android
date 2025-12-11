package com.ft.webview;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;


public class TCWebView extends TCBaseWebView {


    public TCWebView(@NonNull @NotNull Context context) {
        super(context);
    }

    public TCWebView(@NonNull @NotNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TCWebView(@NonNull @NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TCWebView(@NonNull @NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
    }
}
