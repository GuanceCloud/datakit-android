package com.ft.tests;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.application.MockApplication;
import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.ft.AllTests.hasPrepare;
import static org.junit.Assert.assertEquals;

/**
 * BY huangDianHua
 * DATE:2020-01-10 15:20
 * Description: 用户绑定与解绑测试类
 */
@RunWith(AndroidJUnit4.class)
public class BindUserTest extends BaseTest {
    Context context = null;

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        startSyncTask();
        context = MockApplication.getContext();
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN))
                .setNeedBindUser(true)
                .enableAutoTrack(true);
        FTSdk.install(ftSDKConfig);
        FTDBManager.get().delete();
    }

    /**
     * 测试没有绑定用户的情况，埋点事件将不会上传到服务器，数据会一直保存在数据库中
     *
     * @throws InterruptedException
     */
    @Test
    public void notBindUserDataSync() throws InterruptedException {
        //解绑用户
        FTSdk.get().unbindUserData();
        //间隔15秒查询数据库数据，因为上传的逻辑最长可能要10秒后执行
        Thread.sleep(15000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(1, recordDataList.size());
    }

    /**
     * 测试绑定用户情况下，数据应该上传，本地数据库应该清空
     *
     * @throws InterruptedException
     */
    @Test
    public void bindUserDataSync() throws InterruptedException {
        bindUserData("FT-TEST");
        FTAutoTrack.startApp();
        Thread.sleep(15000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(0, recordDataList.size());
    }

    /**
     * 先解绑用户，在绑定用户
     *
     * @throws InterruptedException
     */
    @Test
    public void unbindAndBindDataSync() throws InterruptedException {
        FTSdk.get().unbindUserData();
        Thread.sleep(5000);
        bindUserData("FT-TEST");
        FTAutoTrack.startApp();
        Thread.sleep(15000);
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
