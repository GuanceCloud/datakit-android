package com.ft;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;

/**
 * For testing purposes
 *
 * @author Brandon
 */
public class DebugMainActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView scrollView = findViewById(R.id.scrollView);
        ViewGroup content = (ViewGroup) scrollView.getChildAt(0);
        Button acceptanceButton = new Button(this);
        acceptanceButton.setText(R.string.historical_anr_acceptance_entry);
        acceptanceButton.setOnClickListener(view -> startActivity(
                new Intent(this, HistoricalAnrAcceptanceActivity.class)));
        content.addView(acceptanceButton, 0);
    }

}
