package com.ft;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTrack;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.SyncCallback;

import org.json.JSONException;
import org.json.JSONObject;


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
                    .setFlowProduct("demo13")
                    .setMonitorType(MonitorType.ALL);//设置监控项
            FTSdk.install(ftSDKConfig);
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
