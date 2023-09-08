package com.ft;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ft.sdk.FTLogger;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.annotation.IgnoreAOP;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.reflect.ReflectUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.service.TestService;
import com.ft.utils.RequestUtils;

import okhttp3.Request;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        }

        findViewById(R.id.main_mock_crash_btn).setOnClickListener(v -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int i = 1 / 0;
                }
            }).start();
        });
        findViewById(R.id.main_mock_crash_native_btn).setOnClickListener(v -> {
            ReflectUtils.reflectCrashAndGetExceptionMessage();
        });
        findViewById(R.id.main_mock_page_jump_btn).setOnClickListener(v -> {
            startActivity(new Intent(this, WebViewActivity.class));
        });
        findViewById(R.id.main_mock_click_btn).setOnClickListener(v -> {
        });
        findViewById(R.id.main_mock_log_btn).setOnClickListener(v -> {
            Log.e(TAG, "console log");
            FTLogger.getInstance().logBackground("custom Log", Status.ERROR);
        });

        findViewById(R.id.main_mock_okhttp_btn).setOnClickListener(v -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //通过查看请求头查看是否替换调用 OkHttpClient.Builder.build 方法成功
                    Request request = RequestUtils.requestUrl(BuildConfig.TRACE_URL);
                    LogUtils.d(TAG, "header=" + request.headers().toString());
                }
            }).start();
        });

        findViewById(R.id.main_mock_ui_block_btn).setOnClickListener(v -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.main_mock_anr_btn).setOnClickListener(v -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        findViewById(R.id.main_view_loop_test).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FirstActivity.class));

        });

        findViewById(R.id.main_manual_rum_btn).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ManualRUMActivity.class));

        });
        findViewById(R.id.main_aop_ignore).setOnClickListener(new View.OnClickListener() {
            @Override
            @IgnoreAOP
            public void onClick(View v) {

            }
        });

        findViewById(R.id.main_start_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(MainActivity.this, TestService.class);
                startService(serviceIntent);
            }
        });

        findViewById(R.id.main_lazy_init).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.LAZY_INIT) {
                    DemoApplication.initFTSDK();
                } else {
                    Toast.makeText(MainActivity.this, "需要先更改 LAZY_INIT 为 true", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.main_shut_down).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FTSdk.shutDown();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int grantResult : grantResults) {
                if (grantResult == PERMISSION_DENIED) {
//                    finish();
                }
            }
        }
    }

}
