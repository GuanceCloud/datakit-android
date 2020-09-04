package com.ft;

import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.db.FTDBManager;

import org.junit.After;

/**
 * author: huangDianHua
 * time: 2020/9/4 17:11:40
 * description:
 */
public class BaseTest {
    @After
    public void tearDown() {
        FTDBManager.get().delete();
        try {
            FTSdk.get().shutDown();
        }catch (Exception e){e.printStackTrace();}
    }
}
