package com.ft;

import android.widget.Button;
import android.widget.RadioGroup;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.FTMonitorConfig;
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
 * Description: 用户绑定与解绑测试类
 */
public class BindUserTest {
    private String accesskey_id = "accid";
    private String accessKey_secret = "accsk";
    private String serverUrl = "http://10.100.64.106:19457/v1/write/metrics";

    /**
     * 在测试用例执行之前需要删除数据库中已经存在的数据
     */
    @Before
    public void deleteTableData() {
        FTDBManager.get().delete();
    }

    /**
     * 初始化 SDK 参数
     */
    @Before
    public void initSDK() {
        FTMonitorConfig.get().clear();
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
                .setMonitorType(MonitorType.BATTERY | MonitorType.NETWORK);//设置监控项
        FTSdk.install(ftSDKConfig, FTApplication.getApplication());
    }

    /**
     * 测试没有绑定用户的情况，埋点事件将不会上传到服务器，数据会一直保存在数据库中
     * @throws InterruptedException
     */
    @Test
    public void notBindUserDataSync() throws InterruptedException {
        //解绑用户
        FTSdk.get().unbindUserData();
        //产生一个埋点事件
        FTAutoTrack.startApp();
        //间隔15秒查询数据库数据，因为上传的逻辑最长可能要10秒后执行
        Thread.sleep(15000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(1, recordDataList.size());
    }

    /**
     * 测试绑定用户情况下，数据应该上传，本地数据库应该清空
     * @throws InterruptedException
     */
    @Test
    public void bindUserDataSync() throws InterruptedException {
        bindUserData("FT-TEST");
        FTAutoTrack.startApp();
        Thread.sleep(20000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(0, recordDataList.size());
    }

    /**
     * 先解绑用户，在绑定用户
     * @throws InterruptedException
     */
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
        FTSdk.get().bindUserData(name, "123456", jsonObject);
    }
}
