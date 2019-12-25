package com.ft.sdk;

import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.manager.SyncTaskManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * BY huangDianHua
 * DATE:2019-12-25 16:38
 * Description:
 */
public class FTDBManagerTest {
    private int repeatTime = 100;

    /**
     * 在运行测试用例前先删除之前数据库中存在的数据
     */
    @Before
    public void deleteTableData() {
        FTDBManager.get().delete();
    }

    /**
     * 向数据库中插入一条数据
     *
     * @throws JSONException
     */
    private void insertData() throws JSONException {
        JSONObject tags = new JSONObject();
        tags.put("name", "json");
        JSONObject values = new JSONObject();
        values.put("value", "success");
        SyncTaskManager.get().setRunning(true);
        FTTrack.getInstance().track("TestEvent", tags, values);
    }

    /**
     * 测试向数据库中插入一条数据是否插入成功
     *
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test
    public void TrackDataTest() throws InterruptedException, JSONException {
        insertData();
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(1, recordDataList.size());
    }

    /**
     * 测试向数据库中插入 {@link #repeatTime} 条数据，和查到的数据是否相同
     *
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void TrackMoreDataTest() throws JSONException, InterruptedException {
        int i = 0;
        while (i < repeatTime) {
            insertData();
            i++;
            Thread.sleep(100);
        }
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(repeatTime, recordDataList.size());
    }

    /**
     * 测试通过数据的ID来删除数据
     *
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test
    public void deleteByIdsTest() throws JSONException, InterruptedException {
        insertData();
        insertData();
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(2, recordDataList.size());
        List<String> integers = new ArrayList<>();
        for (RecordData recordData : recordDataList) {
            integers.add("" + recordData.getId());
        }
        FTDBManager.get().delete(integers);
        List<RecordData> recordDataList1 = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(0, recordDataList1.size());
    }
}
