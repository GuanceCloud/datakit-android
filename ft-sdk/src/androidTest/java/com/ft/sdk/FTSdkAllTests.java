package com.ft.sdk;

import com.ft.sdk.tests.ConfigTest;
import com.ft.sdk.tests.DataFormatTest;
import com.ft.sdk.tests.DataSyncTest;
import com.ft.sdk.tests.DeviceUtilsTest;
import com.ft.sdk.tests.FTDBManagerTest;
import com.ft.sdk.tests.LogGlobalContextTest;
import com.ft.sdk.tests.LogTest;
import com.ft.sdk.tests.MonitorConfigTest;
import com.ft.sdk.tests.RUMGlobalContextTest;
import com.ft.sdk.tests.RUMTest;
import com.ft.sdk.tests.RUMUserBindTest;
import com.ft.sdk.tests.SDKGlobalContextTest;
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
        ConfigTest.class,
        DataFormatTest.class,
        DeviceUtilsTest.class,
        FTDBManagerTest.class,
        LogGlobalContextTest.class,
        LogTest.class,
        MonitorConfigTest.class,
        RUMTest.class,
        RUMUserBindTest.class,
        SDKGlobalContextTest.class,
        SDKRunStateTest.class,
        TraceHeaderTest.class}
)
public class FTSdkAllTests {
    public static boolean hasPrepare;
}
