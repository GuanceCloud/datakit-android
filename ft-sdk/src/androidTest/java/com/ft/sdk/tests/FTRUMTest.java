package com.ft.sdk.tests;

import android.view.textclassifier.TextLinks;

import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTTraceHandler;
import com.ft.sdk.FTTraceManager;
import com.ft.sdk.SyncTaskManager;
import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;
import com.ft.sdk.garble.utils.Utils;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import okhttp3.Request;

public class FTRUMTest {


    public static final String FIRST_VIEW = "FirstView";
    public static final String SECOND_VIEW = "SecondView";
    public static final String ROOT = "root";
    public static final String ACTION = "action";
    public static final String ACTION_TYPE_NAME = "action test";

    @BeforeClass
    public static void setUp() throws Exception {
        stopSyncTask();
    }

    @Test
    public void actionGenerateTest() throws Exception {
        FTRUMGlobalManager.get().startAction(ACTION, ACTION_TYPE_NAME);
        checkActionClose();
        waitForInThreadPool();
        ArrayList<ActionBean> list = FTDBManager.get().querySumAction(0);

        ActionBean action = list.get(0);
        Assert.assertTrue(action.isClose());
        Assert.assertEquals(action.getActionName(), ACTION);
        Assert.assertEquals(action.getActionType(), ACTION_TYPE_NAME);


    }


    @Test
    public void viewGenerateTest() throws Exception {
        FTRUMGlobalManager.get().onCreateView(FIRST_VIEW, 6000000L);
        FTRUMGlobalManager.get().startView(FIRST_VIEW);
        FTRUMGlobalManager.get().stopView();


        FTRUMGlobalManager.get().startView(SECOND_VIEW);

        waitForInThreadPool();

        ArrayList<ViewBean> list = FTDBManager.get().querySumView(0);
        Assert.assertEquals(list.size(), 2);

        ViewBean firstView = list.get(0);
        ViewBean secondView = list.get(1);

        Assert.assertNotEquals(firstView.getId(), secondView.getId());

        Assert.assertTrue(firstView.isClose());
        Assert.assertFalse(secondView.isClose());

        Assert.assertTrue(firstView.getLoadTime() > 0);
        Assert.assertEquals(-1, secondView.getLoadTime());

    }

    @Test
    public void viewMapTest() throws InterruptedException {
        FTRUMGlobalManager.get().startView(FIRST_VIEW);
        FTRUMGlobalManager.get().stopView();

        FTRUMGlobalManager.get().startView(SECOND_VIEW);
        FTRUMGlobalManager.get().stopView();

        waitForInThreadPool();

        ArrayList<ViewBean> list = FTDBManager.get().querySumView(0);
        Assert.assertEquals(list.size(), 2);

        ViewBean firstView = list.get(0);
        ViewBean secondView = list.get(1);

        Assert.assertEquals(firstView.getViewName(), FIRST_VIEW);
        Assert.assertEquals(secondView.getViewName(), SECOND_VIEW);

        Assert.assertEquals(firstView.getViewReferrer(), ROOT);
        Assert.assertEquals(secondView.getViewReferrer(), FIRST_VIEW);


    }

    @Test
    public void viewActionSumTest() throws Exception {
        FTRUMGlobalManager.get().startView(FIRST_VIEW);

        FTRUMGlobalManager.get().startAction(ACTION, ACTION_TYPE_NAME);

        Request request = new Request.Builder().url("https://www.baidu.com").build();

        String resourceId = Utils.identifyRequest(request);

        Assert.assertFalse(resourceId.isEmpty());

        FTRUMGlobalManager.get().startResource(resourceId);
        FTRUMGlobalManager.get().stopResource(resourceId);

        FTRUMGlobalManager.get().addError("error", "error msg", ErrorType.JAVA, AppState.RUN);


        FTRUMGlobalManager.get().addLongTask("longtask", 1000000L);

        checkActionClose();
        waitForInThreadPool();

        FTRUMGlobalManager.get().stopView();


        ArrayList<ActionBean> actionList = FTDBManager.get().querySumAction(0);
        Assert.assertEquals(actionList.size(), 1);

        ActionBean action = actionList.get(0);
        Assert.assertEquals(action.getResourceCount(), 1);
        Assert.assertEquals(action.getErrorCount(), 1);
        Assert.assertEquals(action.getLongTaskCount(), 1);


        ArrayList<ViewBean> viewList = FTDBManager.get().querySumView(0);
        ViewBean viewBean = viewList.get(0);

        Assert.assertEquals(viewBean.getResourceCount(), 1);
        Assert.assertEquals(viewBean.getErrorCount(), 1);
        Assert.assertEquals(viewBean.getLongTaskCount(), 1);
        Assert.assertEquals(viewBean.getActionCount(), 1);
    }



    private void checkActionClose() throws Exception {
        Thread.sleep(1000);
        Whitebox.invokeMethod(FTRUMGlobalManager.get(), "checkActionClose");

    }

    private void generateRumData() throws Exception {
        Thread.sleep(1000);
        Whitebox.invokeMethod(FTRUMGlobalManager.get(), "generateRumData");

    }

    private static void stopSyncTask() throws Exception {
        Whitebox.invokeMethod(SyncTaskManager.get(), "setRunning", true);

    }

    @After
    public void tearDown() {
        FTDBManager.get().delete();
    }

    private void waitForInThreadPool() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        EventConsumerThreadPool.get().execute(() -> {
            countDownLatch.countDown();
        });
        countDownLatch.await();
    }


}
