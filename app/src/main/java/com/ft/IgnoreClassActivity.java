package com.ft;


import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.annotation.IgnoreAOP;

@IgnoreAOP
public class IgnoreClassActivity extends NameTitleActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ignore);

        findViewById(R.id.ignore_click_btn).setOnClickListener(this);

        findViewById(R.id.ignore_not_work_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 这里不会忽略
            }
        });
    }


    @Override
    public void onClick(View v) {
        //会被忽略
    }
}
