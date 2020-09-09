package com.ft.sdk.tests;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.garble.utils.DeviceUtils;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

/**
 * BY huangDianHua
 * DATE:2019-12-16 18:40
 * Description:
 */
public class DeviceUtilsTest {
    private Context getContext(){
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void getSDKUUid(){
        assertNotEquals("",DeviceUtils.getSDKUUid(getContext()));
    }
    @Test
    public void getUuid(){
        assertNotEquals("",DeviceUtils.getUuid(getContext()));
    }

    @Test
    public void requestPermission(){
        assertNotEquals("",DeviceUtils.getImei(getContext()));
    }
}
