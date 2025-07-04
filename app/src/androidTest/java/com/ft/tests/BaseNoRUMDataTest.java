package com.ft.tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;

import com.ft.BaseTest;
import com.ft.R;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDBManager;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Base class for RUM data generation, used to verify that different scenario configurations obtain different data
 */
abstract public class BaseNoRUMDataTest extends BaseTest {

    /**
     *
     * Simulated test for RUM View data generation
     *
     * @throws Exception
     */
    @Test
    public void viewGenerateTest() throws Exception {
        invokeGenerateRumData();
        Thread.sleep(1000);
        List<SyncData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0,
                DataType.RUM_APP);

        Assert.assertTrue(recordDataList.isEmpty());


    }

    /**
     *
     * Simulated test for RUM Action click event
     *
     * @throws Exception
     */
    @Test
    public void rumCLickTest() throws Exception {
        Thread.sleep(2000);

        onView(ViewMatchers.withId(R.id.main_mock_click_btn)).perform(ViewActions.scrollTo()).perform(click());

        invokeCheckActionClose();
        Thread.sleep(1000);

        List<SyncData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        Assert.assertTrue(recordDataList.isEmpty());
    }

    /**
     *  Simulated test for RUM Resource network resource request
     * @throws Exception
     */
    @Test
    public void resourceInterceptorTest() throws Exception {
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.main_mock_okhttp_btn)).perform(ViewActions.scrollTo()).perform(click());
        invokeCheckActionClose();

        Thread.sleep(2000);

        List<SyncData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        Assert.assertTrue(recordDataList.isEmpty());


    }
}
