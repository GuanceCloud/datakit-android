package com.ft.sdk;

import com.ft.sdk.tests.DeviceUtilsTest;
import com.ft.sdk.tests.FTDBManagerTest;

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
        DeviceUtilsTest.class}
)
public class FTSdkAllTests {
}
