package com.ft.sdk;

import com.ft.sdk.tests.ConfigTest;
import com.ft.sdk.tests.DataFormatTest;
import com.ft.sdk.tests.DeviceUtilsTest;
import com.ft.sdk.tests.FTDBManagerTest;
import com.ft.sdk.tests.RUMTest;
import com.ft.sdk.tests.SDKRunStateTest;
import com.ft.sdk.tests.TraceHeaderTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * BY huangDianHua
 * DATE:2019-12-18 14:31
 * Description:
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        FTDBManagerTest.class,
        DeviceUtilsTest.class,
        RUMTest.class,
        SDKRunStateTest.class,
        ConfigTest.class,
        DataFormatTest.class,
        TraceHeaderTest.class}
)
public class FTSdkAllTests {
    public static boolean hasPrepare;
}
