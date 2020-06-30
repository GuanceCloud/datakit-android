package com.ft;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTrack;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.SyncCallback;
import com.ft.sdk.garble.bean.KeyEventBean;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.ObjectBean;
import com.ft.sdk.garble.http.NetWorkTracingListener;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

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
            List<LogBean> logBeans = new ArrayList<>();
            LogBean logBean = new LogBean("android_custom_log", "这是一条多行日志\n 这是第二行日志\n 日志结束", System.currentTimeMillis());
            logBeans.add(logBean);
            FTTrack.getInstance().logImmediate(logBeans, new SyncCallback() {
                @Override
                public void onResponse(int code, String response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Main2Activity.this, "code:" + code + " response:" + response, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            List<LogBean> logBeans1 = new ArrayList<>();
            LogBean logBean1 = new LogBean("android_custom_log", "后台同步事件", System.currentTimeMillis());
            logBeans1.add(logBean1);
            FTTrack.getInstance().logBackground(logBeans1);
        });
        findViewById(R.id.jump8).setOnClickListener(v -> {
            KeyEventBean keyEventBean = new KeyEventBean("这是一个独特的事件", System.currentTimeMillis());
            keyEventBean.setContent("这是这个独特事件的内容");
            FTTrack.getInstance().keyEventImmediate(Collections.singletonList(keyEventBean), new SyncCallback() {
                @Override
                public void onResponse(int code, String response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Main2Activity.this, "code:" + code + " response:" + response, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        });
        findViewById(R.id.jump9).setOnClickListener(v -> {
            ObjectBean objectBean = new ObjectBean("custom_data", "objectBackground");
            List<ObjectBean> list = new ArrayList<>();
            list.add(objectBean);
            /**FTTrack.getInstance().objectImmediate(list, new SyncCallback() {
            @Override public void onResponse(int code, String response) {
            runOnUiThread(new Runnable() {
            @Override public void run() {
            Toast.makeText(Main2Activity.this,"code:"+code+" response:"+response,Toast.LENGTH_LONG).show();
            }
            });
            }
            });*/

            FTTrack.getInstance().objectBackground(list);
        });

        findViewById(R.id.jump10).setOnClickListener(v -> {
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


                    NetWorkTracingListener listener = new NetWorkTracingListener();
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(listener)
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
                        Log.i("test", Objects.requireNonNull(response.body()).string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        });
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
}
