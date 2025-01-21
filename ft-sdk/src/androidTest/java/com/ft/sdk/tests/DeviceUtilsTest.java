package com.ft.sdk.tests;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.NetUtils;
import com.ft.sdk.garble.utils.Utils;

import org.junit.Test;

/**
 * BY huangDianHua
 * DATE:2019-12-16 18:40
 * Description:设备相关信息验证
 */
public class DeviceUtilsTest {
    private Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    /***
     * 获取设备 UUid
     */
    @Test
    public void getUuid() {
        assertNotEquals("", DeviceUtils.getUuid(getContext()));
    }

    @Test
    public void isNetworkAvailable() {
        assertTrue(NetUtils.isNetworkAvailable(getContext()));
    }

}
