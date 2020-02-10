package com.ft;

import android.widget.Button;
import android.widget.RadioGroup;

import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.FTAutoTrackConfig;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 *  测试 白名单
 */
public class WhiteListTest extends SDKBaseTest {
    /**
     * 初始化 白名单 SDK 配置
     * @param ftSDKConfig
     */
    @Override
    public void customSDKPrams(FTSDKConfig ftSDKConfig) {
        ftSDKConfig.enableAutoTrack(true)
                .setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type |
                        FTAutoTrackType.APP_END.type |
                        FTAutoTrackType.APP_START.type)//设置埋点事件类型的白名单
                .setWhiteActivityClasses(Arrays.asList(MainActivity.class))//设置埋点页面的白名单
                .setWhiteViewClasses(Arrays.asList(Button.class, RadioGroup.class));
    }

    /**
     * 测试白名单配置是否成功
     */
    @Test
    public void testWhiteList() {
        Assert.assertTrue(FTAutoTrackConfig.get().isAutoTrack());
        Assert.assertTrue(FTAutoTrackConfig.get().enableAutoTrackType(FTAutoTrackType.APP_CLICK));
        Assert.assertTrue(FTAutoTrackConfig.get().isOnlyAutoTrackActivity(MainActivity.class));
        Assert.assertFalse(FTAutoTrackConfig.get().isOnlyAutoTrackActivity(Main2Activity.class));
        Assert.assertFalse(FTAutoTrackConfig.get().isOnlyView(Button.class));
    }
}
