package com.ft;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * author: huangDianHua
 * time: 2020/8/27 17:36:40
 * description:
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        LogEventTest.class,//Log-事件日志测试
        LogTest.class,//Log-控制台日志,短时间批量插入丢弃策略测试
        LogTrackObjectTraceTest.class,//数据同步-log、track、object、trace
        BindUserTest.class,//用户绑定测试
        FTAutoTrackTest.class,//AutoTrack 测试
        FTInitParamTest.class,//Base-Property 参数测试
        LocationTest.class,//定位测试
        MonitorTest.class,//监控测试
        OaidTest.class,//oaid 测试
        SDKRunStateTest.class,//SDK 生命周期测试
        TraceTest.class,//trace 测试
        ExceptionTest.class//Log-崩溃日志测试
})
public class TestEntrance {
    public static boolean hasPrepare;
}
