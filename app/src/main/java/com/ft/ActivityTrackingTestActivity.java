package com.ft;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ft.sdk.garble.utils.LogUtils;

/**
 * Test Activity for View Activity Tracking Handler
 * This activity is used to test the setViewActivityTrackingHandler functionality
 * by providing a target activity for navigation testing
 */
public class ActivityTrackingTestActivity extends AppCompatActivity {

    private TextView infoTextView;
    private Button backButton;
    private Button testButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tracking_test);

        initViews();
        setupClickListeners();
        displayTestInfo();
    }

    /**
     * Initialize view references
     */
    private void initViews() {
        infoTextView = findViewById(R.id.info_text_view);
        backButton = findViewById(R.id.back_button);
        testButton = findViewById(R.id.test_button);
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use finish() to properly close the activity
                finish();
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This will trigger action tracking
                testButton.setText("Button Clicked!");
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Handle back button press to avoid DeadObjectException
        super.onBackPressed();
    }

    /**
     * Display test information
     */
    private void displayTestInfo() {
        StringBuilder info = new StringBuilder();
        info.append("ViewTrackingTestActivity\n\n");
        info.append("This activity is used to test:\n");
        info.append("• setViewActivityTrackingHandler\n");
        info.append("• Activity view tracking\n");
        info.append("• Custom view name generation\n\n");
        
        // Display extra data if available
        if (getIntent() != null && getIntent().hasExtra("test_source")) {
            info.append("Test Source: ").append(getIntent().getStringExtra("test_source")).append("\n");
        }
        if (getIntent() != null && getIntent().hasExtra("test_timestamp")) {
            long timestamp = getIntent().getLongExtra("test_timestamp", 0);
            info.append("Test Timestamp: ").append(timestamp).append("\n");
        }
        
        info.append("\nExpected Behavior:\n");
        info.append("• Custom handler should be called\n");
        info.append("• View name should be customized\n");
        info.append("• Custom properties should be added\n");
        
        infoTextView.setText(info.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // This will trigger view tracking when the activity becomes visible
        LogUtils.d("CustomViewActivityTrackingHandler","activity onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // This will trigger view tracking when the activity becomes invisible
    }
}
