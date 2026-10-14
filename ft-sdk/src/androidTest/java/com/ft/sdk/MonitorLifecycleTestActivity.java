package com.ft.sdk;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

/** Foreground host for the isolated metrics lifecycle device test. */
public class MonitorLifecycleTestActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        TextView text = new TextView(this);
        text.setText("Metrics lifecycle verification");
        setContentView(text);
    }
}
