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
 * RUM 数据生成基础累，用于检验不同场景配置，获取到不同的数据
 */
abstract public class BaseNoRUMDataTest extends BaseTest {

    /**
     *
     * RUM View 数据生成模拟测试
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
     * RUM Action 点击事件模拟测试
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
     *  RUM Resource 网络资源请求模拟测试
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
