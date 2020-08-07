package com.ft;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.amitshekhar.DebugDB;
import com.amitshekhar.debug.encrypt.sqlite.DebugDBEncryptFactory;
import com.amitshekhar.debug.sqlite.DebugDBFactory;
import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTNetWorkTracerInterceptor;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTrack;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.SyncCallback;
import com.ft.sdk.garble.bean.LogData;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.pm.PackageManager.PERMISSION_DENIED;


public class Main2Activity extends AppCompatActivity {

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
        findViewById(R.id.bindUser).setOnClickListener(v -> {
            JSONObject exts = new JSONObject();
            try {
                exts.put("sex", "male");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            FTSdk.get().bindUserData("jack", "007", exts);
        });
        findViewById(R.id.unbindUser).setOnClickListener(v -> {
            FTSdk.get().unbindUserData();
        });
        findViewById(R.id.changeUser).setOnClickListener(v -> {
            JSONObject exts = new JSONObject();
            try {
                exts.put("sex", "female");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            FTSdk.get().bindUserData("Rose", "000", exts);
        });
        findViewById(R.id.jump).setOnClickListener(v -> {
            trackImmediate();
        });
        findViewById(R.id.sync_data_background).setOnClickListener(v -> {
            JSONObject field = new JSONObject();
            try {
                field.put("login", "yes");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            FTTrack.getInstance().trackBackground("mobile_tracker_artificial", null, field);
        });
        findViewById(R.id.jump2).setOnClickListener(v -> {
            trackImmediateErr();
        });

        findViewById(R.id.jump4).setOnClickListener(v -> {
            FTSdk.get().shutDown();
        });
        findViewById(R.id.jump5).setOnClickListener(v -> {
            FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(this, AccountUtils.ACCESS_SERVER_URL),
                    true,
                    AccountUtils.getProperty(this, AccountUtils.ACCESS_KEY_ID),
                    AccountUtils.getProperty(this, AccountUtils.ACCESS_KEY_SECRET))
                    .setXDataKitUUID("ft-dataKit-uuid-002")
                    .setUseOAID(true)//设置 OAID 是否可用
                    .setDebug(true)//设置是否是 debug
                    .setGeoKey(true, AccountUtils.getProperty(this, AccountUtils.GEO_KEY))
                    .setNeedBindUser(false)//是否需要绑定用户信息
                    .enableAutoTrack(true)//设置是否开启自动埋点
                    .setEventFlowLog(true)
                    .setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type |
                            FTAutoTrackType.APP_END.type |
                            FTAutoTrackType.APP_START.type)//设置埋点事件类型的白名单
                    //.setWhiteActivityClasses(Arrays.asList(MainActivity.class, Main2Activity.class))//设置埋点页面的白名单
                    //.setWhiteViewClasses(Arrays.asList(Button.class, RadioGroup.class))
                    .setMonitorType(MonitorType.ALL);//设置监控项
            FTSdk.install(ftSDKConfig);
        });

        findViewById(R.id.jump6).setOnClickListener(v -> {
            FTSdk.startLocation("", new SyncCallback() {
                @Override
                public void onResponse(int code, String response) {
                    Toast.makeText(Main2Activity.this, "code=" + code + ",response=" + response, Toast.LENGTH_LONG).show();
                }
            });
        });
        findViewById(R.id.jump7).setOnClickListener(v -> {
            FTTrack.getInstance().logBackground(Thread.currentThread().getName() + ":" + Main2Activity.class.getName(), Status.CRITICAL);
        });
        findViewById(R.id.jump8).setOnClickListener(v -> {
            List<LogData> logDataList = new ArrayList<>();
            logDataList.add(new LogData(Thread.currentThread().getName() + ":" + System.currentTimeMillis() + ":" + Main2Activity.class.getName(), Status.CRITICAL));
            logDataList.add(new LogData(Thread.currentThread().getName() + ":" + System.currentTimeMillis() + ":" + Main2Activity.class.getName(), Status.CRITICAL));
            logDataList.add(new LogData(Thread.currentThread().getName() + ":" + System.currentTimeMillis() + ":" + Main2Activity.class.getName(), Status.CRITICAL));
            logDataList.add(new LogData(Thread.currentThread().getName() + ":" + System.currentTimeMillis() + ":" + Main2Activity.class.getName(), Status.CRITICAL));
            FTTrack.getInstance().logBackground(logDataList);
        });
        findViewById(R.id.jump10).setOnClickListener(v -> {
            requestUrl("http://www.weather.com.cn/data/sk/101010100.html");
            requestUrl("https://www.baidu.com");
        });

        findViewById(R.id.jump11).setOnClickListener(v -> {
            requestUrl("https://error.url");
        });

        findViewById(R.id.jump12).setOnClickListener(v -> {
            requestUrl("https://www.google.com");
        });

        findViewById(R.id.jump13).setOnClickListener(v -> {
            requestUrl("https://www.baidu.com");
            requestUrl("https://www.google.com");
        });
        findViewById(R.id.jump14).setOnClickListener(v -> {
            AtomicLong atomicLong = new AtomicLong(0);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Log.d("LogManager", "data=======" + atomicLong.getAndIncrement());
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        });
    }

    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new FTNetWorkTracerInterceptor())
            .build();

    private void requestUrl(@NonNull String url) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Request.Builder builder = new Request.Builder().url(url)
                        .method(RequestMethod.GET.name(), null);
                try {
                    client.newCall(builder.build()).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void traceWithWriteMetrics() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String accessKey_id = "accid";
                String accessKey_secret = "accsk";

                Date currentTime = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.UK);
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                String gmtString = sdf.format(currentTime);


                FTNetWorkTracerInterceptor interceptor = new FTNetWorkTracerInterceptor();
                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(interceptor)
                        .build();


                long currentTimeNm = System.currentTimeMillis() * 1000000L;
                String body = "test_client," +
                        "device_uuid=$deviceUUID," +
                        "application_identifier=com.app.identifier," +
                        "application_name=应用名称," +
                        "sdk_version=1.0.0-alpha-6881c11," +
                        "imei=123456789012347," +
                        "os=Android," +
                        "os_version=10," +
                        "locale=zh_CN#Hans," +
                        "device_band=Google," +
                        "device_model=Pixel\\ 2," +
                        "display=1980*1080," +
                        "carrier=CDMA" +
                        " event=\"launch\" " + currentTimeNm + "\n";

                String contentType = "text/plain; charset=utf-8";

                String sign = Utils.getHMacSha1(accessKey_secret, "POST" + "\n" + Utils.contentMD5Encode(body) + "\n" + contentType + "\n" + gmtString);
                String authorization = "DWAY " + accessKey_id + ":" + sign;
                Request.Builder builder = new Request.Builder().url("http://10.100.64.198:9528/v1/write/metrics?token=tkn_4c4f9f29f39c493199bb5abe7df6af21")
                        .addHeader("Date", gmtString)
                        .addHeader("Authorization", authorization)
                        .addHeader("X-Datakit-UUID", DeviceUtils.getUuid(Main2Activity.this))
                        .method(RequestMethod.POST.name(), RequestBody.create(null, body));


                try {
                    Response response = client.newCall(builder.build()).execute();
                    LogUtils.i("test", Objects.requireNonNull(response.body()).string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public void trackImmediate() {
        JSONObject field = new JSONObject();
        try {
            field.put("login", "yes");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FTTrack.getInstance().trackImmediate("mobile_tracker_artificial", null, field, new SyncCallback() {
            @Override
            public void onResponse(int code, String response) {
                Log.d("onResponse", "code=" + code + ",response=" + response);
            }
        });
    }

    public void trackImmediateErr() {
        FTTrack.getInstance().trackImmediate("mobile_tracker_artificial", null, null, new SyncCallback() {
            @Override
            public void onResponse(int code, String response) {
                Log.d("onResponse", "code=" + code + ",response=" + response);
            }
        });

        FTTrack.getInstance().trackImmediate(null, null, null, new SyncCallback() {
            @Override
            public void onResponse(int code, String response) {
                Log.d("onResponse", "code=" + code + ",response=" + response);
            }
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
