package com.ft.sdk.garble;

import com.ft.sdk.FTSDKConfig;

/**
 * BY huangDianHua
 * DATE:2020-01-09 17:26
 * Description:流程图配置类
 */
public class FTUserActionConfig {
    private static FTUserActionConfig ftUserActionConfig;
    //流程图日志上报功能
    private boolean enableUserAction;

    private FTUserActionConfig() {
    }

    public static FTUserActionConfig get() {
        if (ftUserActionConfig == null) {
            ftUserActionConfig = new FTUserActionConfig();
        }
        return ftUserActionConfig;
    }

    public void initParams(FTSDKConfig ftsdkConfig) {
        enableUserAction = ftsdkConfig.isEnableTraceUserAction();
    }


    public boolean isEnableTraceUserAction() {
        return enableUserAction;
    }

    public static void release() {
        ftUserActionConfig = null;
    }
}
