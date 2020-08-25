package com.ft.sdk;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * BY huangDianHua
 * DATE:2019-12-18 14:31
 * Description:
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({DeviceUtilsTest.class,
        FTTrackTest.class,
        GenericsUtilsTest.class,
        FTDBManagerTest.class,
        ThreadPoolutilsTest.class,
        UtilsTest.class})
public class FTSdkTest {
}
