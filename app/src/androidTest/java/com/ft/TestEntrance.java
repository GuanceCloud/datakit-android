package com.ft;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * author: huangDianHua
 * time: 2020/8/27 17:36:40
 * description:
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({BindUserTest.class,
        FTAutoTrackTest.class,
        FTInitParamTest.class,
        LocationTest.class,
        LogEventTest.class,
        LogTest.class,
        LogTrackObjectTraceTest.class,
        MonitorTest.class,
        OaidTest.class,
        SDKRunStateTest.class,
        TraceTest.class,
        ExceptionTest.class})
public class TestEntrance {
    public static boolean hasPrepare;
}
