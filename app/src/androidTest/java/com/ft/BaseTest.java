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
 * description: Test case base class, used for permission application inheritance in test cases
 */
public class BaseTest extends FTBaseTest {

    /**
     * Apply for {@link Manifest.permission#READ_PHONE_STATE} permission
     */
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_PHONE_STATE
    );

    /**
     * Test completed, delete and clear data
     */
    @After
    public void tearDown() {
        //Database deletion
        FTDBManager.get().delete();
        //Shutdown SDK
        FTSdk.shutDown();
    }


}
