package com.ft.sdk.tests;


import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import com.ft.sdk.EnvType;
import com.ft.sdk.FTLogger;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.ErrorMonitorType;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.Constants;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.CheckUtils;
import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Data synchronization verification, verify whether there are data backlog issues or data request error issues
 *
 * @author Brandon
 */
public class DataSyncTest extends FTBaseTest {

    private static final String EMPTY_RESPONSE = "";
    private MockWebServer mMockWebServer;

    @Before
    public void setUp() throws IOException {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        mMockWebServer = new MockWebServer();

        MockResponse mockResponse = new MockResponse();
        mockResponse.setBody(EMPTY_RESPONSE);
        mockResponse.setResponseCode(HttpURLConnection.HTTP_OK);
        mMockWebServer.enqueue(mockResponse);
        mMockWebServer.play();

        FTSDKConfig ftSDKConfig = FTSDKConfig
                .builder(mMockWebServer.getUrl("/").toString())
                .setDebug(true)//Set whether it is debug
                .setAutoSync(false)
                .setEnv(EnvType.GRAY);

        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setExtraMonitorTypeWithError(ErrorMonitorType.ALL.getValue())
                .setEnableTrackAppCrash(true)
                .setRumAppId(TEST_FAKE_RUM_ID)
                .setEnableTrackAppUIBlock(true)
                .setEnableTraceUserAction(true)
                .setEnableTraceUserView(true)
                .setEnableTraceUserResource(true)
        );

        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .setEnableCustomLog(true));

        FTSdk.initTraceWithConfig(new FTTraceConfig().setEnableAutoTrace(true));
    }

    /**
     * {@link DataType#RUM_APP} type data synchronization verification. Verify data writing and data upload
     * @throws Exception
     */
    @Test
    public void rumTest() throws Exception {

        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTRUMGlobalManager.get().stopView();

        waitEventConsumeInThreadPool();
        Thread.sleep(2000);
        int except1 = CheckUtils.getCount(DataType.RUM_APP, Constants.FT_MEASUREMENT_RUM_VIEW, 0);
        Assert.assertTrue(except1 > 0);
        resumeSyncTask();
        executeSyncTask();
        Thread.sleep(1000);
        int except2 = CheckUtils.getCount(DataType.RUM_APP, Constants.FT_MEASUREMENT_RUM_VIEW, 0);
        Assert.assertEquals(0, except2);
    }


    /**
     * Log data writing test
     *
     * @throws InterruptedException
     */
    @Test
    public void logUpdateDataTest() throws InterruptedException {
        //Generate a log data
        FTLogger.getInstance().logBackground("----logUpdateDataTest----", Status.CRITICAL);
        //Inserted in thread pool, there is a certain time delay, set 5 seconds wait time here
        Thread.sleep(3000);
        uploadData(DataType.LOG);
    }


    /**
     * Data synchronization test, data will be deleted from cache after synchronization is completed
     *
     * @throws InterruptedException
     */
    @Test
    public void logSyncTest() throws Exception {
        resumeSyncTask();
        String logContent = "----logUploadTest----";

        FTLogger.getInstance().logBackground(logContent, Status.CRITICAL);
        Thread.sleep(12000);
        int except = CheckUtils.getCount(DataType.LOG, logContent, 0);
        Assert.assertEquals(0, except);
    }


    @After
    public void tearDown() {
        super.tearDown();
        try {
            mMockWebServer.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
