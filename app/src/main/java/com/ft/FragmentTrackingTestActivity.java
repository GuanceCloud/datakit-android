package com.ft;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ft.sdk.garble.utils.LogUtils;

/**
 * Test Activity for Fragment Tracking Handler
 * This activity is used to test the setViewFragmentTrackingHandler functionality
 * by providing multiple fragments for testing
 */
public class FragmentTrackingTestActivity extends AppCompatActivity {

    private TextView infoTextView;
    private Button backButton;
    private Button switchFragmentBtn;
    private Button addFragmentBtn;
    private Button removeFragmentBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_tracking_test);

        initViews();
        setupClickListeners();
        setupInitialFragment();
        displayTestInfo();
    }

    /**
     * Initialize view references
     */
    private void initViews() {
        infoTextView = findViewById(R.id.info_text_view);
        backButton = findViewById(R.id.back_button);
        switchFragmentBtn = findViewById(R.id.switch_fragment_btn);
        addFragmentBtn = findViewById(R.id.add_fragment_btn);
        removeFragmentBtn = findViewById(R.id.remove_fragment_btn);
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        switchFragmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment();
            }
        });

        addFragmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragment();
            }
        });

        removeFragmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFragment();
            }
        });
    }

    /**
     * Setup initial fragment
     */
    private void setupInitialFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        FragmentTest1 initialFragment = new FragmentTest1();
        transaction.add(R.id.fragment_container, initialFragment, "FragmentTest1");
        transaction.commit();
        
        LogUtils.d("FragmentTrackingTestActivity", "Initial fragment added: FragmentTest1");
    }

    /**
     * Switch to different fragment
     */
    private void switchFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        if (currentFragment instanceof FragmentTest1) {
            FragmentTest2 newFragment = new FragmentTest2();
            transaction.replace(R.id.fragment_container, newFragment, "FragmentTest2");
            LogUtils.d("FragmentTrackingTestActivity", "Switching to FragmentTest2");
        } else if (currentFragment instanceof FragmentTest2) {
            FragmentTest3 newFragment = new FragmentTest3();
            transaction.replace(R.id.fragment_container, newFragment, "FragmentTest3");
            LogUtils.d("FragmentTrackingTestActivity", "Switching to FragmentTest3");
        } else {
            FragmentTest1 newFragment = new FragmentTest1();
            transaction.replace(R.id.fragment_container, newFragment, "FragmentTest1");
            LogUtils.d("FragmentTrackingTestActivity", "Switching to FragmentTest1");
        }
        
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Add a new fragment
     */
    private void addFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        FragmentTest4 newFragment = new FragmentTest4();
        transaction.add(R.id.fragment_container, newFragment, "FragmentTest4");
        transaction.addToBackStack(null);
        transaction.commit();
        
        LogUtils.d("FragmentTrackingTestActivity", "Added FragmentTest4");
    }

    /**
     * Remove current fragment
     */
    private void removeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        
        if (currentFragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(currentFragment);
            transaction.commit();
            
            LogUtils.d("FragmentTrackingTestActivity", "Removed fragment: " + currentFragment.getClass().getSimpleName());
        }
    }

    /**
     * Display test information
     */
    private void displayTestInfo() {
        StringBuilder info = new StringBuilder();
        info.append("FragmentTrackingTestActivity\n\n");
        info.append("This activity is used to test:\n");
        info.append("• setViewFragmentTrackingHandler\n");
        info.append("• Fragment view tracking\n");
        info.append("• Custom fragment name generation\n\n");
        
        // Display extra data if available
        if (getIntent() != null && getIntent().hasExtra("test_source")) {
            info.append("Test Source: ").append(getIntent().getStringExtra("test_source")).append("\n");
        }
        if (getIntent() != null && getIntent().hasExtra("test_timestamp")) {
            long timestamp = getIntent().getLongExtra("test_timestamp", 0);
            info.append("Test Timestamp: ").append(timestamp).append("\n");
        }
        
        info.append("\nExpected Behavior:\n");
        info.append("• Custom handler should be called for each fragment\n");
        info.append("• Fragment names should be customized\n");
        info.append("• Custom properties should be added\n");
        info.append("• Fragment lifecycle events should be tracked\n");
        
        infoTextView.setText(info.toString());
    }

    @Override
    public void onBackPressed() {
        // Handle back button press to avoid DeadObjectException
        super.onBackPressed();
    }

    /**
     * Test Fragment 1
     */
    public static class FragmentTest1 extends Fragment {
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LogUtils.d("FragmentTest1", "onCreate called");
        }
        
        @Override
        public void onStart() {
            super.onStart();
            LogUtils.d("FragmentTest1", "onStart called");
        }
        
        @Override
        public void onResume() {
            super.onResume();
            LogUtils.d("FragmentTest1", "onResume called");
        }
        
        @Override
        public void onPause() {
            super.onPause();
            LogUtils.d("FragmentTest1", "onPause called");
        }
    }

    /**
     * Test Fragment 2
     */
    public static class FragmentTest2 extends Fragment {
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LogUtils.d("FragmentTest2", "onCreate called");
        }
        
        @Override
        public void onStart() {
            super.onStart();
            LogUtils.d("FragmentTest2", "onStart called");
        }
        
        @Override
        public void onResume() {
            super.onResume();
            LogUtils.d("FragmentTest2", "onResume called");
        }
        
        @Override
        public void onPause() {
            super.onPause();
            LogUtils.d("FragmentTest2", "onPause called");
        }
    }

    /**
     * Test Fragment 3
     */
    public static class FragmentTest3 extends Fragment {
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LogUtils.d("FragmentTest3", "onCreate called");
        }
        
        @Override
        public void onStart() {
            super.onStart();
            LogUtils.d("FragmentTest3", "onStart called");
        }
        
        @Override
        public void onResume() {
            super.onResume();
            LogUtils.d("FragmentTest3", "onResume called");
        }
        
        @Override
        public void onPause() {
            super.onPause();
            LogUtils.d("FragmentTest3", "onPause called");
        }
    }

    /**
     * Test Fragment 4
     */
    public static class FragmentTest4 extends Fragment {
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LogUtils.d("FragmentTest4", "onCreate called");
        }
        
        @Override
        public void onStart() {
            super.onStart();
            LogUtils.d("FragmentTest4", "onStart called");
        }
        
        @Override
        public void onResume() {
            super.onResume();
            LogUtils.d("FragmentTest4", "onResume called");
        }
        
        @Override
        public void onPause() {
            super.onPause();
            LogUtils.d("FragmentTest4", "onPause called");
        }
    }
}
