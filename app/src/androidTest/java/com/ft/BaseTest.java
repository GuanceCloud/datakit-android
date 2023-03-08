package com.ft;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.test.base.FTBaseTest;

import org.junit.After;
import org.junit.Rule;

/**
 * author: huangDianHua
 * time: 2020/9/4 17:11:40
 * description:测试用例基础类，用于继承测试用例的权限申请
 */
public class BaseTest extends FTBaseTest {

    /**
     * 申请 {@link Manifest.permission#READ_PHONE_STATE} 权限
     */
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_PHONE_STATE
    );

    /**
     * 测试完毕，删除清空数据
     */
    @After
    public void tearDown() {
        //数据库删除
        FTDBManager.get().delete();
        //关闭 SDK
        FTSdk.shutDown();
    }


}
