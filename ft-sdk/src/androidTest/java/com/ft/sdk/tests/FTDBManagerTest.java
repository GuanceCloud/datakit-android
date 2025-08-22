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
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.db.base.DatabaseHelper;
import com.ft.sdk.garble.utils.Constants;
import com.ft.test.base.FTBaseTest;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-12-25 16:38
 * Description: Database creation, data insertion, data deletion test
 */
public class FTDBManagerTest extends FTBaseTest {
    private static final int repeatTime = 100;

    /**
     * Test database creation
     */
    @Test
    public void createDatabase() {
        String testDBName = "test.db";
        int dbVersion = 1;
        SQLiteOpenHelper helper = DatabaseHelper.getInstance(FTApplication.getApplication(), testDBName, dbVersion);
        assertEquals(testDBName, helper.getDatabaseName());
    }

    /**
     * Test whether inserting a piece of data into the database is successful
     *
     * @throws InterruptedException
     * @throws Exception
     */
    @Test
    public void insertDataTest() throws Exception {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID));
        insertData();
        //Because data insertion is an asynchronous operation, an interval needs to be set to be able to query the data
        Thread.sleep(2000);
        List<SyncData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(1, recordDataList.size());
    }

    /**
     * Test inserting {@link #repeatTime} pieces of data into the database, and whether the found data is the same
     *
     * @throws Exception
     * @throws InterruptedException
     */
    @Test
    public void insertMultiDataTest() throws Exception {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID));
        int i = 0;
        while (i < repeatTime) {
            insertData();
            i++;
            Thread.sleep(100);
        }
        //Because data insertion is an asynchronous operation, an interval needs to be set to be able to query the data
        Thread.sleep(1000);
        List<SyncData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(repeatTime, recordDataList.size());
    }

    /**
     * Test deleting data by data ID
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
        //Because data insertion is an asynchronous operation, an interval needs to be set to be able to query the data
        Thread.sleep(1000);
        List<SyncData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(2, recordDataList.size());
        List<Long> integers = new ArrayList<>();
        for (SyncData recordData : recordDataList) {
            integers.add(recordData.getId());
        }
        FTDBManager.get().delete(integers, false);
        List<SyncData> recordDataList1 = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(0, recordDataList1.size());
    }

    /**
     * Insert a piece of data into the database
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

        FTDBCachePolicy.get().setCurrentDBSize(Constants.MINI_DB_SIZE_LIMIT);

        assertTrue(FTDBCachePolicy.get().isReachDbLimit());

        assertEquals(FTDBCachePolicy.get().optRUMCachePolicy(10), -1);

        assertEquals(FTDBCachePolicy.get().optLogCachePolicy(10), -10);

        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL)
                .enableLimitWithDbSize(Constants.MINI_DB_SIZE_LIMIT)
                .setDbCacheDiscard(DBCacheDiscard.DISCARD_OLDEST));

        assertEquals(FTDBCachePolicy.get().optRUMCachePolicy(10), 1);

        assertEquals(FTDBCachePolicy.get().optLogCachePolicy(10), 10);
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
