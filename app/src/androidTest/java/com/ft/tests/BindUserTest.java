package com.ft.tests;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.application.MockApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.SyncJsonData;
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
        FTDBManager.get().delete();

        context = MockApplication.getContext();
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL))
                .setDebug(true);
        FTSdk.install(ftSDKConfig);
    }

    /**
     * @throws InterruptedException
     */
    @Test
    public void notBindUserDataSync() throws InterruptedException, JSONException {
        //解绑用户
        FTSdk.get().unbindUserData();
        simpleTrackData();
        //间隔15秒查询数据库数据，因为上传的逻辑最长可能要10秒后执行
        Thread.sleep(15000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(0, recordDataList.size());
    }

    /**
     * @throws InterruptedException
     */
    @Test
    public void bindUserDataSync() throws InterruptedException, JSONException {
        bindUserData();
        simpleTrackData();
        Thread.sleep(15000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(0, recordDataList.size());
    }

    /**
     * 先解绑用户，在绑定用户
     *
     * @throws InterruptedException
     */
    @Test
    public void unbindAndBindDataSync() throws InterruptedException, JSONException {
        FTSdk.get().unbindUserData();
        Thread.sleep(5000);
        bindUserData();
        simpleTrackData();
        Thread.sleep(15000);
        List<SyncJsonData> recordDataList1 = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(0, recordDataList1.size());
    }

    private void bindUserData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sex", "man");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FTSdk.get().bindUserData("123456");
    }
}
