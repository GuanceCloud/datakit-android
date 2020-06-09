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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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
                field.put("login","yes");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            FTTrack.getInstance().trackBackground("mobile_tracker_artificial", null, field);
        });
        findViewById(R.id.jump2).setOnClickListener(v -> {trackImmediateErr();});

        findViewById(R.id.jump3).setOnClickListener(v->{
            FTTrack.getInstance().trackFlowChart("ft_sdk_android","001","开始",null,1000,null,null);
        });

        findViewById(R.id.jump4).setOnClickListener(v->{
            FTSdk.get().shutDown();
        });
        findViewById(R.id.jump5).setOnClickListener(v->{
            FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(this, AccountUtils.ACCESS_SERVER_URL),
                    true,
                    AccountUtils.getProperty(this, AccountUtils.ACCESS_KEY_ID),
                    AccountUtils.getProperty(this, AccountUtils.ACCESS_KEY_SECRET))
                    .setXDataKitUUID("ft-dataKit-uuid-002")
                    .setUseOAID(true)//设置 OAID 是否可用
                    .setDebug(true)//设置是否是 debug
                    .setGeoKey(true,AccountUtils.getProperty(this, AccountUtils.GEO_KEY))
                    .setNeedBindUser(false)//是否需要绑定用户信息
                    .enableAutoTrack(true)//设置是否开启自动埋点
                    .setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type |
                            FTAutoTrackType.APP_END.type |
                            FTAutoTrackType.APP_START.type)//设置埋点事件类型的白名单
                    //.setWhiteActivityClasses(Arrays.asList(MainActivity.class, Main2Activity.class))//设置埋点页面的白名单
                    //.setWhiteViewClasses(Arrays.asList(Button.class, RadioGroup.class))
                    .setOpenFlowChart(true)
                    //.setProduct("demo13")
                    .setMonitorType(MonitorType.ALL);//设置监控项
            FTSdk.install(ftSDKConfig);
        });

        findViewById(R.id.jump6).setOnClickListener(v -> {
            FTSdk.startLocation("", new SyncCallback() {
                @Override
                public void onResponse(int code, String response) {
                    Toast.makeText(Main2Activity.this,"code="+code+",response="+response,Toast.LENGTH_LONG).show();
                }
            });
        });

        findViewById(R.id.jump7).setOnClickListener(v -> {
            List<LogBean> logBeans = new ArrayList<>();
            LogBean logBean = new LogBean("android_custom_log","这是一条多行日志\n 这是第二行日志\n 日志结束",System.currentTimeMillis());
            logBeans.add(logBean);
            FTTrack.getInstance().logImmediate(logBeans, new SyncCallback() {
                @Override
                public void onResponse(int code, String response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Main2Activity.this,"code:"+code+" response:"+response,Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            List<LogBean> logBeans1 = new ArrayList<>();
            LogBean logBean1 = new LogBean("android_custom_log","后台同步事件",System.currentTimeMillis());
            logBeans1.add(logBean1);
            FTTrack.getInstance().logBackground(logBeans1);
        });
        findViewById(R.id.jump8).setOnClickListener(v ->{
            KeyEventBean keyEventBean = new KeyEventBean("这是一个独特的事件",System.currentTimeMillis());
            keyEventBean.setContent("这是这个独特事件的内容");
            FTTrack.getInstance().keyEventImmediate(Collections.singletonList(keyEventBean), new SyncCallback() {
                @Override
                public void onResponse(int code, String response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Main2Activity.this,"code:"+code+" response:"+response,Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        });
        findViewById(R.id.jump9).setOnClickListener(v->{
            ObjectBean objectBean = new ObjectBean("custom_data","objectBackground");
            List<ObjectBean> list = new ArrayList<>();
            list.add(objectBean);
            /**FTTrack.getInstance().objectImmediate(list, new SyncCallback() {
                @Override
                public void onResponse(int code, String response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Main2Activity.this,"code:"+code+" response:"+response,Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });*/

            FTTrack.getInstance().objectBackground(list);
        });
    }
    public void trackImmediate(){
        JSONObject field = new JSONObject();
        try {
            field.put("login","yes");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FTTrack.getInstance().trackImmediate("mobile_tracker_artificial", null, field, new SyncCallback() {
            @Override
            public void onResponse(int code, String response) {
                Log.d("onResponse","code="+code+",response="+response);
            }
        });
    }

    public void trackImmediateErr(){
        FTTrack.getInstance().trackImmediate("mobile_tracker_artificial", null, null, new SyncCallback() {
            @Override
            public void onResponse(int code, String response) {
                Log.d("onResponse","code="+code+",response="+response);
            }
        });

        FTTrack.getInstance().trackImmediate(null, null, null, new SyncCallback() {
            @Override
            public void onResponse(int code, String response) {
                Log.d("onResponse","code="+code+",response="+response);
            }
        });
    }
}
