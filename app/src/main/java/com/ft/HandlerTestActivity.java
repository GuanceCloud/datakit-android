package com.ft;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ft.sdk.ActionSourceType;
import com.ft.sdk.FTAutoTrack;

import java.util.HashMap;

import com.ft.sdk.garble.utils.LogUtils;

/**
 * Activity for testing FTRUMConfig handlers:
 * - setActionTrackingHandler
 * - setViewActivityTrackingHandler
 * - setViewFragmentTrackingHandler
 * <p>
 * This activity demonstrates how to use custom handlers to customize
 * action and view tracking behavior
 */
public class HandlerTestActivity extends NameTitleActivity {

    private static final String TAG = "HandlerTestActivity";
    private TextView logTextView;
    private Button testActionHandlerBtn;
    private Button testViewHandlerBtn;
    private Button testFragmentHandlerBtn;
    private Button clearLogBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handler_test);

        initViews();
        setupClickListeners();
        setupFragment();
    }

    /**
     * Initialize all view references
     */
    private void initViews() {
        logTextView = findViewById(R.id.log_text_view);
        testActionHandlerBtn = findViewById(R.id.test_action_handler_btn);
        testViewHandlerBtn = findViewById(R.id.test_view_handler_btn);
        testFragmentHandlerBtn = findViewById(R.id.test_fragment_handler_btn);
        clearLogBtn = findViewById(R.id.clear_log_btn);
    }

    /**
     * Setup click listeners for all buttons
     */
    private void setupClickListeners() {
        // Test Action Tracking Handler
        testActionHandlerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testActionTrackingHandler();
            }
        });

        // Test View Activity Tracking Handler
        testViewHandlerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testViewActivityTrackingHandler();
            }
        });

        // Test Fragment Tracking Handler
        testFragmentHandlerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testFragmentTrackingHandler();
            }
        });

        // Clear log
        clearLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLog();
            }
        });
    }

    /**
     * Setup test fragment
     */
    private void setupFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        TestFragment testFragment = new TestFragment();
        transaction.add(R.id.fragment_container, testFragment, "TestFragment");
        transaction.commit();
    }

    /**
     * Test Action Tracking Handler functionality
     * This demonstrates how custom action tracking can be implemented
     */
    private void testActionTrackingHandler() {
        addLog("=== Testing Action Tracking Handler ===");

        // Simulate different action types
        testActionType(ActionSourceType.CLICK_VIEW, "Normal Button Click");
        testActionType(ActionSourceType.CLICK_LIST_ITEM, "List Item Click");
        testActionType(ActionSourceType.CLICK_RADIO_BUTTON, "Radio Button Click");
        testActionType(ActionSourceType.CLICK_DIALOG_BUTTON, "Dialog Button Click");
        testActionType(ActionSourceType.CLICK_TAB, "Tab Click");
        testActionType(ActionSourceType.CLICK_EXPAND_LIST_ITEM, "Expandable List Item Click");
        testActionType(ActionSourceType.CLICK_EXPAND_GROUP_ITEM, "Expandable Group Click");
        testActionType(ActionSourceType.CLICK_BACK, "Back Button Click");
        testActionType(ActionSourceType.LAUNCH_COLD, "Cold Launch");
        testActionType(ActionSourceType.LAUNCH_HOT, "Hot Launch");

        addLog("Action tracking test completed");
    }

    /**
     * Test a specific action type
     */
    private void testActionType(ActionSourceType actionType, String description) {
        addLog("Testing: " + description + " (" + actionType + ")");

        // Create test data
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("test_timestamp", System.currentTimeMillis());
        extraData.put("test_description", description);
        extraData.put("test_source", "HandlerTestActivity");

        // Simulate action tracking
        FTAutoTrack.clickView(testActionHandlerBtn, actionType, extraData);

        addLog("  - Action tracked with extra data: " + extraData.size() + " items");
    }

        /**
     * Test View Activity Tracking Handler functionality
     * This demonstrates how custom view tracking can be implemented for activities
     */
    private void testViewActivityTrackingHandler() {
        addLog("=== Testing View Activity Tracking Handler ===");
        
        addLog("Current Activity: " + this.getClass().getSimpleName());
        addLog("Starting activity navigation test...");
        
        try {
            // Create a simple test activity to navigate to
            Intent intent = new Intent(this, ActivityTrackingTestActivity.class);
            intent.putExtra("test_source", "HandlerTestActivity");
            intent.putExtra("test_timestamp", System.currentTimeMillis());
            
            addLog("Navigating to ViewTrackingTestActivity...");
            startActivity(intent);
            
            addLog("Activity navigation initiated - check logs for handler calls");
        } catch (Exception e) {
            addLog("Error starting activity: " + e.getMessage());
        }
    }

        /**
     * Test Fragment Tracking Handler functionality
     * This demonstrates how custom fragment tracking can be implemented
     */
    private void testFragmentTrackingHandler() {
        addLog("=== Testing Fragment Tracking Handler ===");
        
        addLog("Current Activity: " + this.getClass().getSimpleName());
        addLog("Starting fragment navigation test...");
        
        try {
            // Create a test activity to navigate to for fragment testing
            Intent intent = new Intent(this, FragmentTrackingTestActivity.class);
            intent.putExtra("test_source", "HandlerTestActivity");
            intent.putExtra("test_timestamp", System.currentTimeMillis());
            
            addLog("Navigating to FragmentTrackingTestActivity...");
            startActivity(intent);
            
            addLog("Fragment navigation initiated - check logs for handler calls");
        } catch (Exception e) {
            addLog("Error starting fragment test activity: " + e.getMessage());
        }
    }

    /**
     * Add log message to the log view
     */
    private void addLog(String message) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String logEntry = "[" + timestamp + "] " + message + "\n";

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logTextView.append(logEntry);
                // Auto-scroll to bottom
                final int scrollAmount = logTextView.getLayout().getLineTop(logTextView.getLineCount()) - logTextView.getHeight();
                if (scrollAmount > 0) {
                    logTextView.scrollTo(0, scrollAmount);
                }
            }
        });
    }

    /**
     * Clear the log view
     */
    private void clearLog() {
        logTextView.setText("");
        addLog("Log cleared");
    }

        /**
     * Test Fragment for testing fragment tracking
     */
    public static class TestFragment extends Fragment {
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Fragment lifecycle events will be tracked by the custom handler
            LogUtils.d("TestFragment", "onCreate called");
        }
        
        @Override
        public void onResume() {
            super.onResume();
            // Fragment resume events will be tracked by the custom handler
            LogUtils.d("TestFragment", "onResume called");
        }
        
        @Override
        public void onStart() {
            super.onStart();
            LogUtils.d("TestFragment", "onStart called");
        }
        
        @Override
        public void onPause() {
            super.onPause();
            LogUtils.d("TestFragment", "onPause called");
        }
    }

    /**
     * Second Test Fragment for testing fragment replacement
     */
    public static class TestFragment2 extends Fragment {
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Fragment lifecycle events will be tracked by the custom handler
            LogUtils.d("TestFragment2", "onCreate called");
        }
        
        @Override
        public void onResume() {
            super.onResume();
            // Fragment resume events will be tracked by the custom handler
            LogUtils.d("TestFragment2", "onResume called");
        }
        
        @Override
        public void onStart() {
            super.onStart();
            LogUtils.d("TestFragment2", "onStart called");
        }
        
        @Override
        public void onPause() {
            super.onPause();
            LogUtils.d("TestFragment2", "onPause called");
        }
    }
} 