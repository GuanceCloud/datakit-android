package com.ft;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        findViewById(R.id.jump).setOnClickListener(v -> {
        });
        findViewById(R.id.jump2).setOnClickListener(v -> {});
    }
}
