package com.ft.sdk.tests;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.Utils;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * BY huangDianHua
 * DATE:2019-12-16 18:40
 * Description:设备相关信息验证
 */
public class DeviceUtilsTest {
    private Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    /**
     * 验证设备
     */

    @Test
    public void getSDKUUid() {
        assertNotEquals("", DeviceUtils.getSDKUUid(getContext()));
    }

    @Test
    public void getUuid() {
        assertNotEquals("", DeviceUtils.getUuid(getContext()));
    }

    @Test
    public void isNetworkAvailable() {
        assertTrue(Utils.isNetworkAvailable(getContext()));
    }

}
