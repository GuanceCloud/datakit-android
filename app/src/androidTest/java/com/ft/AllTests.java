package com.ft;


import com.ft.tests.ErrorTraceTest;
import com.ft.tests.ProcessConfigTest;
import com.ft.tests.RUMClickActionTest;
import com.ft.tests.RUMDisableTest;
import com.ft.tests.RUMLaunchActionTest;
import com.ft.tests.RUMNoConfigTest;
import com.ft.tests.RUMViewTest;
import com.ft.tests.ServerConnectTest;
import com.ft.tests.RUMResourceTest;
import com.ft.tests.TraceHeaderTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * author: huangDianHua
 * time: 2020/8/27 17:36:40
 * description: Data test collection
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        // After AGP 8.0, ASM weaving cannot be performed on Log.d method in test cases
//        ConsoleLogTest.class,//Log-Console log, short-term batch insertion discard strategy test
        TraceHeaderTest.class,
        ServerConnectTest.class,//Base-Property parameter test
        ErrorTraceTest.class,//Log-Crash log test
        RUMLaunchActionTest.class,
        RUMClickActionTest.class,
        RUMViewTest.class,
        RUMResourceTest.class,
        RUMDisableTest.class,
        RUMNoConfigTest.class,
        ProcessConfigTest.class
})
public class AllTests {
    public static boolean hasPrepare;
}
