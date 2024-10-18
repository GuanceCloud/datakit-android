package com.ft.sdk.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * BY huangDianHua
 * DATE:2019-12-18 14:31
 * Description:测试合集
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ConfigTest.class,
        DataFormatTest.class,
        DeviceUtilsTest.class,
        FTDBManagerTest.class,
        LogGlobalContextTest.class,
        LogTest.class,
        InnerLogTest.class,
        MonitorConfigTest.class,
        RUMTest.class,
        RUMUserBindTest.class,
        SDKGlobalContextTest.class,
        SDKRunStateTest.class,
        TraceHeaderTest.class,
        SDKDataClearTest.class
}
)
public class FTSdkAllTests {
    public static boolean hasPrepare;
}
