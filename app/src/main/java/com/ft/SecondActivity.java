package com.ft;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends NameTitleActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        findViewById(R.id.second_to_third_btn).setOnClickListener(v -> {

            startActivity(new Intent(SecondActivity.this, ThirdActivity.class));

        });
    }
}
