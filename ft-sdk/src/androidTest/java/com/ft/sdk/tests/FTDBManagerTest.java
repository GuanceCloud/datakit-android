package com.ft.sdk.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.database.sqlite.SQLiteOpenHelper;

import com.ft.sdk.DBCacheDiscard;
import com.ft.sdk.FTApplication;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.db.base.DatabaseHelper;
import com.ft.sdk.garble.utils.Constants;
import com.ft.test.base.FTBaseTest;

import org.junit.Before;
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
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig());
        insertData();
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(2000);
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
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig());
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
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig());
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

    @Test
    public void enableDbLimitCheckLogAndRUM() {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL)
                .enableLimitWithDbSize(Constants.MINI_DB_SIZE_LIMIT)
                .setDbCacheDiscard(DBCacheDiscard.DISCARD));

        FTDBCachePolicy.get().setReachDBLimit(Constants.MINI_DB_SIZE_LIMIT);

        assertTrue(FTDBCachePolicy.get().isReachDbLimit());

        assertEquals(FTDBCachePolicy.get().optRUMCachePolicy(10), -1);

        assertEquals(FTDBCachePolicy.get().optRUMCachePolicy(10), -1);

        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL)
                .enableLimitWithDbSize(Constants.MINI_DB_SIZE_LIMIT)
                .setDbCacheDiscard(DBCacheDiscard.DISCARD_OLDEST));

        assertEquals(FTDBCachePolicy.get().optRUMCachePolicy(10), 0);

        assertEquals(FTDBCachePolicy.get().optRUMCachePolicy(10), 0);
    }

    @Test
    public void dbSizeCheckTest() throws Exception {
        stopSyncTask();

        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL)
                .enableLimitWithDbSize());

        assertTrue(insertDataAndCheckDBIncrease());
    }

    @Test
    public void checkDbLimitDiscard() throws Exception {
        stopSyncTask();

        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL)
                .enableLimitWithDbSize(Constants.MINI_DB_SIZE_LIMIT));
        setDBLimit(10);
        assertFalse(insertDataAndCheckDBIncrease());
    }

    @Test
    public void checkDbLimitDiscardOldest() throws Exception {
        stopSyncTask();
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL)
                .enableLimitWithDbSize(Constants.MINI_DB_SIZE_LIMIT)
                .setDbCacheDiscard(DBCacheDiscard.DISCARD_OLDEST));
        setDBLimit(10);
        assertFalse(insertDataAndCheckDBIncrease());
    }

    private boolean insertDataAndCheckDBIncrease() throws Exception {
        insertOneKBData();
        Thread.sleep(1000);
        long previewSize = getCurrentDBSize();

        insertOneKBData();
        Thread.sleep(1000);
        long currentSize = getCurrentDBSize();
        return currentSize > previewSize;
    }

    private void insertOneKBData() throws Exception {
        HashMap<String, Object> tags = new HashMap<>();
        tags.put("name", "json");
        HashMap<String, Object> values = new HashMap<>();
        values.put("value", "success");
        invokeSyncData(DataType.LOG, LOG_TEST_DATA_1_KB, tags, values);

    }

}
