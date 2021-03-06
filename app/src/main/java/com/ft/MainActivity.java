package com.ft;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ft.sdk.FTLogger;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.reflect.ReflectUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();

    public Request requestUrl(@NonNull String url) {
        Request.Builder builder = new Request.Builder().url(url)
                .method(RequestMethod.GET.name(), null);
        Request request = null;
        try {
            Response response = client.newCall(builder.build()).execute();
            request = response.request();

            ResponseBody responseBody = response.body();
            String string = "";
            if (responseBody != null) {
                string = responseBody.string();
            }
            LogUtils.d(TAG, "url:" + url + "\n" + string);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return request;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_PHONE_STATE
            }, 1);
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
            Log.i(TAG, "console log");
            FTLogger.getInstance().logBackground("custom Log", Status.INFO);
        });

        findViewById(R.id.main_mock_okhttp_btn).setOnClickListener(v -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //????????????????????????????????????????????? OkHttpClient.Builder.build ????????????
                    Request request = requestUrl(AccountUtils.getProperty(MainActivity.this, AccountUtils.TRACE_URL));
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
