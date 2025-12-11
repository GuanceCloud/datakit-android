package com.ft;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ft.sdk.FTLogger;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTRemoteConfigManager;
import com.ft.sdk.FTResourceEventListener;
import com.ft.sdk.FTResourceInterceptor;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceInterceptor;
import com.ft.sdk.garble.annotation.IgnoreAOP;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.reflect.ReflectUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.service.TestService;
import com.ft.utils.RequestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TestService.ACTION_MESSAGE)) {
                boolean installed = intent.getBooleanExtra(TestService.INSTALLED_STATE, false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Button button = findViewById(R.id.main_start_service);
                        button.setText(getString(installed ? R.string.start_service_installed
                                : R.string.start_service_not_install));
                    }
                });
                // Handle received messages here
            }
        }
    };

    public final OkHttpClient client = new OkHttpClient.Builder()
//                                .addInterceptor(new FTTraceInterceptor(new FTTraceInterceptor.HeaderHandler() {
//                                    @Override
//                                    public HashMap<String, String> getTraceHeader(Request request) {
//                                        HashMap<String, String> map = new HashMap<>();
//                                        map.put("custom_header","test_value");
//                                        return map;
//                                    }
//                                }))
            .addInterceptor(new FTTraceInterceptor())
            .addInterceptor(new FTResourceInterceptor(new FTResourceInterceptor.ContentHandlerHelper() {
                @Override
                public void onRequest(Request request, HashMap<String, Object> extraData) {
                    String contentType = request.header("Content-Type");

                    extraData.put("df_request_header", request.headers().toString());
                    if ("application/json".equals(contentType) ||
                            "application/x-www-form-urlencoded".equals(contentType) ||
                            "application/xml".equals(contentType)) {
                        extraData.put("df_request_body", request.body());
                    }

                }

                @Override
                public void onResponse(Response response, HashMap<String, Object> extraData) throws IOException {

                    String contentType = response.header("Content-Type");

                    extraData.put("df_response_header", response.headers().toString());

                    if ("application/json".equals(contentType) ||
                            "application/xml".equals(contentType)) {
                        //copy read partial body to avoid consuming large data
                        ResponseBody body = response.peekBody(33554432);
                        extraData.put("df_response_body", body.string());
                    }
                }

                @Override
                public void onException(Exception e, HashMap<String, Object> extraData) {

                }


            }))
            .eventListenerFactory(new FTResourceEventListener.FTFactory())
            .connectTimeout(10, TimeUnit.SECONDS).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter(TestService.ACTION_MESSAGE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(messageReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(messageReceiver, filter);
        }

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
            FTRUMGlobalManager.get().updateLoadTime(1000000000);

        });
        findViewById(R.id.main_click_test_btn).setOnClickListener(v -> {
            startActivity(new Intent(this, ClickTestActivity.class));
        });
        findViewById(R.id.main_handler_test_btn).setOnClickListener(v -> {
            startActivity(new Intent(this, HandlerTestActivity.class));
        });

        findViewById(R.id.main_dynamic_tags).setOnClickListener(v -> {
            HashMap<String, Object> globalContext = new HashMap<>();
            globalContext.put("global_key", "global_value");
            FTSdk.appendGlobalContext(globalContext);

            HashMap<String, Object> rumGlobalContext = new HashMap<>();
            rumGlobalContext.put("rum_key", "rum_value");
            FTSdk.appendRUMGlobalContext(rumGlobalContext);

            HashMap<String, Object> logGlobalContext = new HashMap<>();
            logGlobalContext.put("log_key", "log_value");
            FTSdk.appendLogGlobalContext(logGlobalContext);

        });
        findViewById(R.id.main_mock_log_btn).setOnClickListener(v -> {
            Log.e(TAG, "console log");
            Log.d(TAG, "console log");
            Log.w(TAG, new Exception());
            Log.println(Log.ERROR, TAG, "println error");
            FTLogger.getInstance().logBackground("custom Log", Status.ERROR);
            FTLogger.getInstance().logBackground("custom status Log", "customType");
        });

        findViewById(R.id.main_mock_okhttp_btn).setOnClickListener(v -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Check request headers to see if OkHttpClient.Builder.build method replacement is successful
                    Request request = RequestUtils.requestUrl(BuildConfig.TRACE_URL);
                    if (request != null) {
                        LogUtils.d(TAG, "header=" + request.headers());

                    }

//                    RequestUtils.requestUrlAsync(BuildConfig.TRACE_URL, new RequestUtils.RequestCallback() {
//                        @Override
//                        public void onSuccess(int code, String response) {
//                            LogUtils.d(TAG, "header=" + response);
//                        }
//
//                        @Override
//                        public void onFailure(IOException e) {
//
//                        }
//                    });

//                    OkGo.<String>get(BuildConfig.TRACE_URL)
//                            .tag(this)
//                            .execute(new StringCallback() {
//                                @Override
//                                public void onSuccess(com.lzy.okgo.model.Response<String> response) {
//                                    response.body();
//                                }
//                            });

//                    try {
//                        OkGo.<String>get(BuildConfig.TRACE_URL+"2")
//                                .tag(this)
//                                .execute().body().string();
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }


                }
            }).start();
        });

        findViewById(R.id.main_mock_okhttp_custom_content_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread() {
                    @Override
                    public void run() {

                        //Custom collection content, need to disable auto collection configuration first
                        // FTTraceConfig.setEnableAutoTrace(false)
                        // FTRUMConfig.setEnableTraceUserResource(false)


                        Request.Builder builder = new Request.Builder()
                                .url(BuildConfig.TRACE_URL)
                                .method(RequestMethod.GET.name(), null);
                        try {
                            Response response = client.newCall(builder.build()).execute();
                            ResponseBody responseBody = response.body();
                            if (responseBody != null) {
                                //Need to consume here, event listener will be called
                                String string = responseBody.string();
                            }

                        } catch (IOException e) {
                            LogUtils.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                }.start();


            }
        });

        findViewById(R.id.main_mock_ui_block_btn).setOnClickListener(v -> {
            try {
                Thread.sleep(1000);
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
                startActivity(new Intent(MainActivity.this, IgnoreClassActivity.class));
            }
        });

        findViewById(R.id.main_start_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(MainActivity.this, TestService.class);
                startService(serviceIntent);
            }
        });

        findViewById(R.id.main_flush_sync_data_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FTSdk.flushSyncData();
            }
        });

        findViewById(R.id.main_high_load_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HighLoadActivity.class));
            }
        });

        findViewById(R.id.main_lazy_init).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (BuildConfig.LAZY_INIT) {
                DemoApplication.initFTSDK(MainActivity.this);
//                } else {
//                    Toast.makeText(MainActivity.this, "Need to change LAZY_INIT to true first", Toast.LENGTH_SHORT).show();
//                }
            }
        });

        findViewById(R.id.main_shut_down).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FTSdk.shutDown();
            }
        });

        findViewById(R.id.main_clear_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FTSdk.clearAllData();
            }
        });

        findViewById(R.id.update_remote_config).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FTSdk.updateRemoteConfig(0, new FTRemoteConfigManager.FetchResult() {
                    @Override
                    public void onResult(boolean success) {
                        LogUtils.d(TAG, "updateRemoteConfig:success->" + success);

                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int grantResult : grantResults) {
                if (grantResult == PERMISSION_DENIED) {
//                    finish();
                }
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        unregisterReceiver(messageReceiver);
    }
}
