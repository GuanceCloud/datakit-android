package com.ft.tests;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.application.MockApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.manager.AsyncCallback;
import com.ft.sdk.garble.utils.LocationUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;

import java.util.concurrent.CountDownLatch;

import static com.ft.AllTests.hasPrepare;

/**
 * author: huangDianHua
 * time: 2020/8/27 11:29:04
 * description:测试定位
 */
@RunWith(AndroidJUnit4.class)
public class LocationTest extends BaseTest {
    Context context;
    FTSDKConfig ftSDKConfig;

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        context = MockApplication.getContext();
        ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setUseOAID(true);//设置 OAID 是否可用
        //关闭数据自动同步操作
        stopSyncTask();
        FTSdk.install(ftSDKConfig);

    }

    @Override
    public void tearDown() {
        super.tearDown();
        address = null;
    }

    Address address;

    @Test
    public void locationTest() throws InterruptedException {
        Thread.sleep(2000);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        address = null;
        LocationUtils.get().startLocationCallBack(new AsyncCallback() {
            @Override
            public void onResponse(int code, String response) {
                address = LocationUtils.get().getAddress();
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        Assert.assertNotNull(address);
    }

    @Test
    public void geoIPAddressTest() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Whitebox.invokeMethod(LocationUtils.get(), "requestGeoIPAddress", new AsyncCallback() {
            @Override
            public void onResponse(int code, String response) {
                if (code == 0) {
                    address = LocationUtils.get().getAddress();
                }
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        Assert.assertNotNull(address);
    }

    @Test
    public void geoCodeAddressTest() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Location location = new Location(LocationManager.NETWORK_PROVIDER);
        location.setLatitude(31.20690892154558);
        location.setLongitude(121.58605522685095);
        Whitebox.invokeMethod(LocationUtils.get(), "requestGeoAddress", location, new AsyncCallback() {
            @Override
            public void onResponse(int code, String response) {
                if (code == 0) {
                    address = LocationUtils.get().getAddress();
                }
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        Assert.assertNotNull(address);
    }
}
