package com.ft;

import android.widget.Button;
import android.widget.RadioGroup;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * BY huangDianHua
 * DATE:2020-01-10 15:20
 * Description:
 */
public class BindUserTest {
    private String accesskey_id = "accid";
    private String accessKey_secret = "accsk";
    private String serverUrl = "http://10.100.64.106:19457/v1/write/metrics";

    @Before
    public void deleteTableData() {
        FTDBManager.get().delete();
    }

    @Before
    public void initSDK() {
        FTSDKConfig ftSDKConfig = FTSDKConfig.Builder(serverUrl,
                true,
                accesskey_id,
                accessKey_secret)
                .setUseOAID(true)//设置 OAID 是否可用
                .setDebug(true)//设置是否是 debug
                .setNeedBindUser(true)//是否需要绑定用户信息
                .enableAutoTrack(true)//设置是否开启自动埋点
                .setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type |
                        FTAutoTrackType.APP_END.type |
                        FTAutoTrackType.APP_START.type)//设置埋点事件类型的白名单
                .setWhiteActivityClasses(Arrays.asList(MainActivity.class, Main2Activity.class))//设置埋点页面的白名单
                .setWhiteViewClasses(Arrays.asList(Button.class, RadioGroup.class))
                .setMonitorType(MonitorType.ALL);//设置监控项
        FTSdk.install(ftSDKConfig);
    }

    @Test
    public void notBindUserDataSync() throws InterruptedException {
        FTSdk.get().unbindUserData();
        FTAutoTrack.startApp();
        Thread.sleep(20000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(1, recordDataList.size());
    }

    @Test
    public void bindUserDataSync() throws InterruptedException {
        bindUserData("FT-TEST");
        FTAutoTrack.startApp();
        Thread.sleep(20000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(0, recordDataList.size());
    }

    @Test
    public void unbindAndBindDataSync() throws InterruptedException {
        FTSdk.get().unbindUserData();
        FTAutoTrack.startApp();
        Thread.sleep(20000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(1, recordDataList.size());
        bindUserData("FT-TEST");
        Thread.sleep(20000);
        List<RecordData> recordDataList1 = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(0, recordDataList1.size());
    }

    private void bindUserData(String name) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sex", "man");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FTSdk.get().bindUserData(name, "123456", null);
    }
}
