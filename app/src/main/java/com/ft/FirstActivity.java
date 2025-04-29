package com.ft;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

/**
 *
 */
public class FirstActivity extends NameTitleActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        findViewById(R.id.first_to_second_btn).setOnClickListener(v -> {
            startActivity(new Intent(FirstActivity.this, SecondActivity.class));
        });

        findViewById(R.id.first_to_repeat_btn).setOnClickListener(v -> {
            startActivity(new Intent(FirstActivity.this, RepeatActivity.class));

        });

        findViewById(R.id.first_to_tabview_btn).setOnClickListener(v -> {
            startActivity(new Intent(FirstActivity.this, TabViewActivity.class));
        });

        findViewById(R.id.fragment_btn).setOnClickListener(v -> {
            startActivity(new Intent(FirstActivity.this, FragmentActivity.class));
        });
    }
}
