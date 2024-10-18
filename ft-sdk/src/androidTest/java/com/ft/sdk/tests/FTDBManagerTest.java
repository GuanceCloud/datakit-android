package com.ft.sdk.tests;

import static org.junit.Assert.assertEquals;

import android.database.sqlite.SQLiteOpenHelper;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.db.base.DatabaseHelper;
import com.ft.test.base.FTBaseTest;

import org.json.JSONException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-12-25 16:38
 * Description: 数据库创库,插入数据,删除数据测试
 */
public class FTDBManagerTest extends FTBaseTest {
    private static final int repeatTime = 100;

    /**
     * 测试创建数据库
     */
    @Test
    public void createDatabase() {
        String testDBName = "test.db";
        int dbVersion = 1;
        SQLiteOpenHelper helper = DatabaseHelper.getInstance(FTApplication.getApplication(), testDBName, dbVersion);
        assertEquals(testDBName, helper.getDatabaseName());
    }

    /**
     * 测试向数据库中插入一条数据是否插入成功
     *
     * @throws InterruptedException
     * @throws Exception
     */
    @Test
    public void insertDataTest() throws Exception {
        insertData();
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(1, recordDataList.size());
    }

    /**
     * 测试向数据库中插入 {@link #repeatTime} 条数据，和查到的数据是否相同
     *
     * @throws Exception
     * @throws InterruptedException
     */
    @Test
    public void insertMultiDataTest() throws Exception {
        int i = 0;
        while (i < repeatTime) {
            insertData();
            i++;
            Thread.sleep(100);
        }
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(repeatTime, recordDataList.size());
    }

    /**
     * 测试通过数据的ID来删除数据
     *
     * @throws Exception
     * @throws InterruptedException
     */
    @Test
    public void deleteDataByIdsTest() throws Exception {
        insertData();
        insertData();
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(2, recordDataList.size());
        List<String> integers = new ArrayList<>();
        for (SyncJsonData recordData : recordDataList) {
            integers.add("" + recordData.getId());
        }
        FTDBManager.get().delete(integers, false);
        List<SyncJsonData> recordDataList1 = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(0, recordDataList1.size());
    }

    /**
     * 向数据库中插入一条数据
     *
     * @throws Exception
     */
    private void insertData() throws Exception {
        HashMap<String, Object> tags = new HashMap<>();
        tags.put("name", "json");
        HashMap<String, Object> values = new HashMap<>();
        values.put("value", "success");
//        SyncTaskManager.get().setRunning(true);
        stopSyncTask();
        invokeSyncData(DataType.LOG, "TestEvent", tags, values);

    }

}
