package com.ft;

//import com.ft.tests.FTAutoTrackTest;
import com.ft.tests.MonitorTest;
import com.ft.tests.RUMActionIdTest;
import com.ft.tests.RUMActionSumTest;
import com.ft.tests.RUMSessionIdTest;
import com.ft.tests.RUMViewIdTest;
import com.ft.tests.RUMViewSumTest;
import com.ft.tests.base.FTInitParamTest;
import com.ft.tests.base.SDKRunStateTest;
import com.ft.tests.ErrorTraceTest;
import com.ft.tests.LogTest;
import com.ft.tests.LogTrackTraceRUMTest;
import com.ft.tests.base.OaidTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * author: huangDianHua
 * time: 2020/8/27 17:36:40
 * description:
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        RUMActionIdTest.class,
        RUMActionSumTest.class,
        RUMSessionIdTest.class,
        RUMViewIdTest.class,
        RUMViewSumTest.class,
        LogTrackTraceRUMTest.class,//数据同步-log、track、trace

        LogTest.class,//Log-控制台日志,短时间批量插入丢弃策略测试
        FTInitParamTest.class,//Base-Property 参数测试
        MonitorTest.class,//监控测试
        OaidTest.class,//oaid 测试

        SDKRunStateTest.class,//SDK 生命周期测试
        ErrorTraceTest.class//Log-崩溃日志测试
})
public class AllTests {
    public static boolean hasPrepare;
}
