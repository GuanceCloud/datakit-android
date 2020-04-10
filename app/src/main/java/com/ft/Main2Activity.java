package com.ft;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTrack;
import com.ft.sdk.garble.SyncCallback;

import org.json.JSONException;
import org.json.JSONObject;


public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        findViewById(R.id.jump).setOnClickListener(v -> {
            FTSdk.get().shutDown();
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
            FTSdk.get().shutDown();
        });
        findViewById(R.id.jump2).setOnClickListener(v -> {trackImmediateErr();});

        findViewById(R.id.jump3).setOnClickListener(v->{
            FTTrack.getInstance().trackFlowChart("ft_sdk_android","001","开始",null,1000,null,null);
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
