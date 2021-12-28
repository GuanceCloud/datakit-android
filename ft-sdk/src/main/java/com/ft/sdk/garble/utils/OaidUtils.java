package com.ft.sdk.garble.utils;

import android.content.Context;
import android.text.TextUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;

/**
 * BY huangDianHua
 * DATE:2019-12-11 16:44
 * Description:
 */
public class OaidUtils {
    public static final String TAG = "OaidUtils";

    private final static int INIT_ERROR_RESULT_DELAY = 1008614;
    private final static int INIT_ERROR_RESULT_OK = 1008610;
    // OAID
    private static String oaid = null;
    private static CountDownLatch countDownLatch;

    /**
     * 获取 oaid 接
     * @param context Context
     * @return oaid
     */
    public static String getOAID(final Context context) {
        try {
            countDownLatch = new CountDownLatch(1);
            if (TextUtils.isEmpty(oaid)) {
                getOAIDReflect(context, 2);
            } else {
                return oaid;
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                //LogUtils.d(e.getMessage());
            }
            //LogUtils.d("CountDownLatch await");
            return oaid;
        } catch (Exception e) {
            //LogUtils.d(e.getMessage());
        }
        return null;
    }

    /**
     * 通过反射获取 OAID，结果返回的 ErrorCode 如下：
     * 1008611：不支持的设备厂商
     * 1008612：不支持的设备
     * 1008613：加载配置文件出错
     * 1008614：获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程
     * 1008615：反射调用出错
     *
     * @param context Context
     * @param retryCount 重试次数
     */
    private static void getOAIDReflect(Context context, int retryCount) {
        try {
            if (retryCount == 0) {
                return;
            }

            Class identifyListener = Class.forName("com.bun.miitmdid.interfaces.IIdentifierListener");
            // 创建 OAID 获取实例
            IdentifyListenerHandler handler = new IdentifyListenerHandler();
            Object iIdentifierListener = Proxy.newProxyInstance(context.getClassLoader(), new Class[]{identifyListener}, handler);
            // 初始化 SDK
            Class<?> midSDKHelper = Class.forName("com.bun.miitmdid.core.MdidSdkHelper");
            Method initSDK = midSDKHelper.getDeclaredMethod("InitSdk", Context.class, boolean.class, identifyListener);
            int errCode = (int) initSDK.invoke(null, context, true, iIdentifierListener);
            if (errCode != INIT_ERROR_RESULT_DELAY && errCode != INIT_ERROR_RESULT_OK) {
                //LogUtils.d("get oaid failed : " + errCode);
                getOAIDReflect(context, --retryCount);
                if (retryCount == 0) {
                    countDownLatch.countDown();
                }
            }

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    //ignore
                }
                countDownLatch.countDown();
            }).start();
        } catch (Exception e) {
            //LogUtils.d(e.getMessage());
            getOAIDReflect(context, --retryCount);
            if (retryCount == 0) {// 对于没有集成 jar 包，尝试后为 0
                countDownLatch.countDown();
            }
        }
    }

    static class IdentifyListenerHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if ("OnSupport".equals(method.getName())) {
                    if ((Boolean) args[0]) {
                        Class<?> idSupplier = Class.forName("com.bun.miitmdid.interfaces.IdSupplier");
                        Method getOAID = idSupplier.getDeclaredMethod("getOAID");
                        oaid = (String) getOAID.invoke(args[1]);
                        //LogUtils.d("oaid:" + oaid);
                    }else{
                        LogUtils.e(TAG,"当前设备不支持 OAID");
                    }

                    countDownLatch.countDown();
                }
            } catch (Exception ex) {
                countDownLatch.countDown();
            }
            return null;
        }
    }

}
