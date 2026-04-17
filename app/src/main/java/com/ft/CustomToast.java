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
        // Get the root view of the Activity
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();

        // Load custom Toast layout
        LayoutInflater inflater = LayoutInflater.from(activity);
        toastView = inflater.inflate(R.layout.custom_toast, decorView, false);

        // Set text
        TextView textView = toastView.findViewById(R.id.toast_text);
        textView.setText(message);

        // Set layout parameters to center the Toast
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER; // Center
        toastView.setLayoutParams(params);

        // Add to DecorView so Session Replay can capture it
        decorView.addView(toastView);

        // Remove after 2 seconds
        toastView.postDelayed(() -> decorView.removeView(toastView), durationMS);
    }
}