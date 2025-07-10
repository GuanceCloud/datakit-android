package com.ft;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class ColoredTextsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colored_texts);
        
        // 设置标题
        setTitle("彩色文本页面");
    }
} 