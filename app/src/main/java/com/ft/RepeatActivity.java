package com.ft;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class RepeatActivity extends NameTitleActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeat);

        findViewById(R.id.repeat_finish_all).setOnClickListener(v -> {
            finishAffinity();
        });

        findViewById(R.id.repeat_to_first_btn).setOnClickListener(v -> {
            startActivity(new Intent(RepeatActivity.this, FirstActivity.class));

        });

        findViewById(R.id.repeat_new_repeat_btn).setOnClickListener(v -> {
            startActivity(new Intent(RepeatActivity.this, RepeatActivity.class));

        });

    }
}
