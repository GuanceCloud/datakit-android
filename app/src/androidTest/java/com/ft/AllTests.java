package com.ft;


import com.ft.tests.ErrorTraceTest;
import com.ft.tests.ConsoleLogTest;
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
 * description:
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ConsoleLogTest.class,//Log-控制台日志,短时间批量插入丢弃策略测试
        ServerConnectTest.class,//Base-Property 参数测试
        ErrorTraceTest.class,//Log-崩溃日志测试
        TraceHeaderTest.class,
        RUMLaunchActionTest.class,
        RUMClickActionTest.class,
        RUMViewTest.class,
        RUMResourceTest.class,
        RUMDisableTest.class,
        RUMNoConfigTest.class,
})
public class AllTests {
    public static boolean hasPrepare;
}
