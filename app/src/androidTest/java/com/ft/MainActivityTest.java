package com.ft;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.manager.SyncTaskManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

/**
 * BY huangDianHua
 * DATE:2019-12-23 17:27
 * Description:
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    /**
     * 在运行测试用例前先删除之前数据库中存在的数据
     */
    @Before
    public void deleteTableData() {
        FTDBManager.get().delete();
        //关闭数据自动同步操作
        SyncTaskManager.get().setRunning(true);
    }

    /**
     * 测试点击某个按钮
     * @throws InterruptedException
     */
    @Test
    public void clickLambdaBtnTest() throws InterruptedException {
        onView(withId(R.id.btn_lam)).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(1, recordDataList.size());
    }

    /**
     * 测试点击多个按钮
     * @throws InterruptedException
     */
    @Test
    public void clickMoreBtnTest() throws InterruptedException {
        onView(withId(R.id.btn_lam)).perform(click());
        onView(withId(R.id.check)).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(2, recordDataList.size());
    }

    /**
     * 测试打开某个Activity
     * @throws InterruptedException
     */
    @Test
    public void jumpActivityTest() throws InterruptedException {
        rule.getActivity().startActivity(new Intent(rule.getActivity(),Main2Activity.class));
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(1, recordDataList.size());
    }

    /**
     * 测试通过按钮点击打开Activity
     * @throws InterruptedException
     */
    @Test
    public void clickJumpActivityTest() throws InterruptedException {
        onView(withId(R.id.showKotlinActivity)).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(2, recordDataList.size());
    }

    /**
     * 测试自动同步操作
     * @throws InterruptedException
     */
    @Test
    public void clickMoreBtnAndSyncTest() throws InterruptedException {
        SyncTaskManager.get().setRunning(false);
        onView(withId(R.id.btn_lam)).perform(click());
        Thread.sleep(100);
        onView(withId(R.id.check)).perform(click());
        Thread.sleep(100);
        onView(withId(R.id.checkbox)).perform(click());
        Thread.sleep(100);
        onView(withId(R.id.checkbox)).perform(click());
        Thread.sleep(100);
        onView(withId(R.id.radio1)).perform(click());
        Thread.sleep(100);
        onView(withId(R.id.radio2)).perform(click());
        Thread.sleep(1000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(6, recordDataList.size());
        Thread.sleep(10*1000);
        List<RecordData> recordDataList1 = FTDBManager.get().queryDataByDescLimit(0);
        assertEquals(0, recordDataList1.size());

    }
}