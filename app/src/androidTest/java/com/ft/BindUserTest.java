package com.ft;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * BY huangDianHua
 * DATE:2020-01-10 15:20
 * Description: 用户绑定与解绑测试类
 */
@RunWith(AndroidJUnit4.class)
public class BindUserTest {

    /**
     * 在测试用例执行之前需要删除数据库中已经存在的数据
     */
    @Before
    public void deleteTableData() {
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
        //产生一个埋点事件
        FTAutoTrack.startApp();
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
        Thread.sleep(20000);
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
