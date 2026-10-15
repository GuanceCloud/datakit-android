package com.ft.launchfixture;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class LaunchActivity extends Activity {
    private static WeakReference<LaunchActivity> current = new WeakReference<>(null);
    private boolean allowDraw;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        current = new WeakReference<>(this);
        allowDraw = !getIntent().getBooleanExtra("gate_first_frame", false);
        TextView content = new TextView(this);
        content.setText("Launch type acceptance\nFirst frame can be held across background/foreground.");
        setContentView(content);
        getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(() -> allowDraw);
    }

    public static void releaseFirstDraw() {
        LaunchActivity activity = current.get();
        if (activity == null) throw new IllegalStateException("No pending Activity");
        LaunchApplication.event("release_first_draw");
        activity.allowDraw = true;
        activity.getWindow().getDecorView().invalidate();
    }
}
