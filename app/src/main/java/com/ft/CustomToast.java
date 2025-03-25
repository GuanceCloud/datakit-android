package com.ft;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CustomToast {

    public static void showToast(Activity activity, String message, int durationMS) {
        View toastView;
        // 获取 Activity 的根视图
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();

        // 加载自定义 Toast 布局
        LayoutInflater inflater = LayoutInflater.from(activity);
        toastView = inflater.inflate(R.layout.custom_toast, decorView, false);

        // 设置文本
        TextView textView = toastView.findViewById(R.id.toast_text);
        textView.setText(message);

        // 设置布局参数，让 Toast 居中
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER; // 居中
        toastView.setLayoutParams(params);

        // 添加到 DecorView， Session Replay 可以捕获
        decorView.addView(toastView);

        // 2 秒后移除
        toastView.postDelayed(() -> decorView.removeView(toastView), durationMS);
    }
}