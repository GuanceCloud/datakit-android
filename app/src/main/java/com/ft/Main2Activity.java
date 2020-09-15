package com.ft;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.amitshekhar.DebugDB;
import com.amitshekhar.debug.encrypt.sqlite.DebugDBEncryptFactory;
import com.amitshekhar.debug.sqlite.DebugDBFactory;

import java.util.concurrent.atomic.AtomicLong;

import static android.content.pm.PackageManager.PERMISSION_DENIED;


public class Main2Activity extends AppCompatActivity {
    public static final String TAG = "Main2Activity";
    boolean logThreadRun = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if (!DebugDB.isServerRunning()) {
            DebugDB.initialize(DemoApplication.getContext(), new DebugDBFactory());
            DebugDB.initialize(DemoApplication.getContext(), new DebugDBEncryptFactory());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA
                    , Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION
                    , Manifest.permission.READ_PHONE_STATE
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.BLUETOOTH
                    , Manifest.permission.BLUETOOTH_ADMIN}, 1);
        }


        Button logThread = findViewById(R.id.mock_period_data_btn);
        logThread.setOnClickListener(v -> {
            logThreadRun = !logThreadRun;
            AtomicLong atomicLong = new AtomicLong(0);
            if (logThreadRun) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (logThreadRun) {
                            Log.d("LogManager", "data=======" + atomicLong.getAndIncrement());
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
            if (logThreadRun) {
                logThread.setText("周期产生日志数据--运行");
            } else {
                logThread.setText("周期产生日志数据--结束");
            }
        });

        findViewById(R.id.mock_one_data_btn).setOnClickListener(v -> {
            Log.d("LogManager", "测试日志数据=======当前时间为" + System.currentTimeMillis());
        });
        findViewById(R.id.mock_crash_btn).setOnClickListener(v -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int i = 1 / 0;
                }
            }).start();
        });
        findViewById(R.id.mock_page_jump_btn).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });
        findViewById(R.id.mock_click_btn).setOnClickListener(v -> {
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int grantResult : grantResults) {
                if (grantResult == PERMISSION_DENIED) {
                    finish();
                }
            }
        }
    }
}
