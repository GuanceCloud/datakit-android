package com.ft;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ft.sdk.ActionSourceType;
import com.ft.sdk.FTAutoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity for testing various ActionSourceType click events
 * This activity provides UI elements to test different types of click events
 * that can be tracked by the FT SDK
 */
public class ClickTestActivity extends NameTitleActivity {

    private static final String TAG = "ClickTestActivity";
    private ListView listViewTest;
    private ExpandableListView expandableListViewTest;
    private TabHost tabHostTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_test);

        // Initialize UI components
        initViews();
        setupClickListeners();
        setupListView();
        setupExpandableListView();
        setupTabHost();
        setupTouchListener();
    }

    /**
     * Initialize all view references
     */
    private void initViews() {
        listViewTest = findViewById(R.id.list_view_test);
        expandableListViewTest = findViewById(R.id.expandable_list_view_test);
        tabHostTest = findViewById(R.id.tab_host_test);
    }

    /**
     * Setup click listeners for all buttons
     */
    private void setupClickListeners() {
        // Normal button click test (CLICK_VIEW)
        findViewById(R.id.btn_normal_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ClickTestActivity.this, "Normal button clicked", Toast.LENGTH_SHORT).show();
                // This will be automatically tracked by FT SDK as CLICK_VIEW
            }
        });

        // Custom click with extra data
        findViewById(R.id.btn_custom_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> extra = new HashMap<>();
                extra.put("custom_data", "test_value");
                extra.put("timestamp", System.currentTimeMillis());
                
                // Manually track click with extra data
                FTAutoTrack.trackViewOnClick(v, extra, true);
                Toast.makeText(ClickTestActivity.this, "Custom click tracked with extra data", Toast.LENGTH_SHORT).show();
            }
        });

        // Radio button group test
        RadioGroup radioGroup = findViewById(R.id.radio_group_test);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // This will be automatically tracked by FT SDK as CLICK_RADIO_BUTTON
                Toast.makeText(ClickTestActivity.this, "Radio button selected: "
                        + checkedId, Toast.LENGTH_SHORT).show();
            }
        });

        // Dialog button test
        findViewById(R.id.btn_show_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTestDialog();
            }
        });


    }

    /**
     * Setup ListView for testing list item clicks (CLICK_LIST_ITEM)
     */
    private void setupListView() {
        List<String> items = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            items.add("List Item " + i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listViewTest.setAdapter(adapter);

        listViewTest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // This will be automatically tracked by FT SDK as CLICK_LIST_ITEM
                Toast.makeText(ClickTestActivity.this, "List item clicked: "
                        + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Setup ExpandableListView for testing expandable list item clicks
     * (CLICK_EXPAND_LIST_ITEM and CLICK_EXPAND_GROUP_ITEM)
     */
    private void setupExpandableListView() {
        // Sample data for expandable list
        List<String> groupData = new ArrayList<>();
        List<List<String>> childData = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            groupData.add("Group " + i);
            List<String> children = new ArrayList<>();
            for (int j = 1; j <= 3; j++) {
                children.add("Child " + i + "." + j);
            }
            childData.add(children);
        }

        ExpandableListAdapter adapter = new ExpandableListAdapter(groupData, childData);
        expandableListViewTest.setAdapter(adapter);

        // Group click listener (CLICK_EXPAND_GROUP_ITEM)
        expandableListViewTest.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // This will be automatically tracked by FT SDK as CLICK_EXPAND_GROUP_ITEM
                Toast.makeText(ClickTestActivity.this, "Group clicked: " + groupPosition, Toast.LENGTH_SHORT).show();
                return false; // Return false to allow default expand/collapse behavior
            }
        });

        // Child click listener (CLICK_EXPAND_LIST_ITEM)
        expandableListViewTest.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                // This will be automatically tracked by FT SDK as CLICK_EXPAND_LIST_ITEM
                Toast.makeText(ClickTestActivity.this, "Child clicked: " + groupPosition + "." + childPosition, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    /**
     * Setup TabHost for testing tab clicks (CLICK_TAB)
     */
    private void setupTabHost() {
        tabHostTest.setup();

        // Add tabs
        TabHost.TabSpec tab1 = tabHostTest.newTabSpec("tab1");
        tab1.setIndicator("Tab 1");
        tab1.setContent(R.id.tab1);
        tabHostTest.addTab(tab1);

        TabHost.TabSpec tab2 = tabHostTest.newTabSpec("tab2");
        tab2.setIndicator("Tab 2");
        tab2.setContent(R.id.tab2);
        tabHostTest.addTab(tab2);

        TabHost.TabSpec tab3 = tabHostTest.newTabSpec("tab3");
        tab3.setIndicator("Tab 3");
        tab3.setContent(R.id.tab3);
        tabHostTest.addTab(tab3);

        // Tab change listener
        tabHostTest.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                // This will be automatically tracked by FT SDK as CLICK_TAB
                Toast.makeText(ClickTestActivity.this, "Tab changed to: " + tabId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Setup touch listener for testing touch events
     */
    private void setupTouchListener() {
        View touchTestView = findViewById(R.id.touch_test_view);
        touchTestView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Manually track touch event
                    FTAutoTrack.trackViewOnTouch(v, event);
                    Toast.makeText(ClickTestActivity.this, "Touch event tracked", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    /**
     * Show test dialog for testing dialog button clicks (CLICK_DIALOG_BUTTON)
     */
    private void showTestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Test Dialog")
                .setMessage("This dialog is used to test CLICK_DIALOG_BUTTON events")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // This will be automatically tracked by FT SDK as CLICK_DIALOG_BUTTON
                        Toast.makeText(ClickTestActivity.this, "Dialog OK button clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // This will be automatically tracked by FT SDK as CLICK_DIALOG_BUTTON
                        Toast.makeText(ClickTestActivity.this, "Dialog Cancel button clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Neutral", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // This will be automatically tracked by FT SDK as CLICK_DIALOG_BUTTON
                        Toast.makeText(ClickTestActivity.this, "Dialog Neutral button clicked", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create().show();
    }



    /**
     * Custom ExpandableListAdapter for testing expandable list functionality
     */
    private static class ExpandableListAdapter extends BaseExpandableListAdapter {
        private final List<String> groupData;
        private final List<List<String>> childData;

        public ExpandableListAdapter(List<String> groupData, List<List<String>> childData) {
            this.groupData = groupData;
            this.childData = childData;
        }

        @Override
        public int getGroupCount() {
            return groupData.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return childData.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupData.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return childData.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = new android.widget.TextView(parent.getContext());
                // Increase left padding to avoid overlap with groupIndicator
                convertView.setPadding(80, 20, 20, 20);
                ((android.widget.TextView) convertView).setTextSize(16);
                ((android.widget.TextView) convertView).setTextColor(android.graphics.Color.BLACK);
            }
            ((android.widget.TextView) convertView).setText(groupData.get(groupPosition));
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = new android.widget.TextView(parent.getContext());
                // Increase left padding for child items to show proper indentation
                convertView.setPadding(120, 15, 20, 15);
                ((android.widget.TextView) convertView).setTextSize(14);
                ((android.widget.TextView) convertView).setTextColor(android.graphics.Color.GRAY);
            }
            ((android.widget.TextView) convertView).setText(childData.get(groupPosition).get(childPosition));
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
} 