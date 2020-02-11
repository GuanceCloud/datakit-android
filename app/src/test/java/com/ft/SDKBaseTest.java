package com.ft;

import android.app.Application;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * 单元测试 SDK 初始化 基类
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class SDKBaseTest {

    @Mock
    Application application;

    @Before
    public void initSDK() {
        MockitoAnnotations.initMocks(this);
        FTSDKConfig ftSDKConfig = FTSDKConfig.Builder(Const.serverUrl,
                true,
                Const.accesskey_id,
                Const.accessKey_secret)
                .setUseOAID(true)//设置 OAID 是否可用
                .setDebug(false);//设置是否是 debug
        customSDKPrams(ftSDKConfig);
        FTSdk.install(ftSDKConfig,application);
    }

    public abstract void customSDKPrams(FTSDKConfig ftSDKConfig);
}
