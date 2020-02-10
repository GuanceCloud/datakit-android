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
 * 黑名单测试
 */
public class BlackListTest extends SDKBaseTest {
    /**
     * 配置 SDK 中的黑名单
     * @param ftSDKConfig
     */
    @Override
    public void customSDKPrams(FTSDKConfig ftSDKConfig) {
        ftSDKConfig.enableAutoTrack(true)
                .setDisableAutoTrackType(FTAutoTrackType.APP_CLICK.type |
                        FTAutoTrackType.APP_END.type |
                        FTAutoTrackType.APP_START.type)//设置埋点事件类型的白名单
                .setBlackActivityClasses(Arrays.asList(MainActivity.class))//设置埋点页面的白名单
                .setBlackViewClasses(Arrays.asList(Button.class, RadioGroup.class));
    }

    /**
     *  测试黑名单设置是否成功
     */
    @Test
    public void testBlackList() {
        Assert.assertTrue(FTAutoTrackConfig.get().isAutoTrack());
        Assert.assertTrue(FTAutoTrackConfig.get().disableAutoTrackType(FTAutoTrackType.APP_CLICK));
        Assert.assertTrue(FTAutoTrackConfig.get().isIgnoreAutoTrackActivity(MainActivity.class));
        Assert.assertFalse(FTAutoTrackConfig.get().isIgnoreAutoTrackActivity(Main2Activity.class));
        Assert.assertFalse(FTAutoTrackConfig.get().isIgnoreView(Button.class));
    }
}
