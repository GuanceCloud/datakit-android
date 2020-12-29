package com.ft;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class ThirdActivity extends NameTitleActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        findViewById(R.id.third_to_first_btn).setOnClickListener(v -> {
            startActivity(new Intent(ThirdActivity.this, FirstActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
            );

        });

    }
}
