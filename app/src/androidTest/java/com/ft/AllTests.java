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
 * description: 数据测试合集
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        // AGP 8.0 以后无法在测试用例中对 Log.d 方法进行 ASM 织入
//        ConsoleLogTest.class,//Log-控制台日志,短时间批量插入丢弃策略测试
        TraceHeaderTest.class,
        ServerConnectTest.class,//Base-Property 参数测试
        ErrorTraceTest.class,//Log-崩溃日志测试
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
