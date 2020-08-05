package com.ft.sdk.garble.utils;

import com.ft.sdk.garble.FTExceptionHandler;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.HttpUrl;

/**
 * author: huangDianHua
 * time: 2020/8/5 20:44:31
 * description: SkyWalking sw8 头部拼接字段
 */
public class SkyWalkingUtils {
    private static AtomicInteger increasingNumber = new AtomicInteger(0);
    private String sw8;
    private String newTraceId;
    public SkyWalkingUtils(String traceID,String sampled,long requestTime,HttpUrl url){
        synchronized (SkyWalkingUtils.class) {//防止多线程 increasingNumber 不安顺序增加
            if (increasingNumber.get() < 9999) {
                increasingNumber.getAndIncrement();
            } else {
                increasingNumber.set(1);
            }
            createSw8Head(traceID, sampled, requestTime, url);
        }
    }

    public String getSw8() {
        return sw8;
    }

    public String getNewTraceId() {
        return newTraceId;
    }

    private void createSw8Head(String traceID, String sampled, long requestTime, HttpUrl url){
        String parentTraceID = traceID + "." + Thread.currentThread().getId() + "." + requestTime + String.format(Locale.getDefault(), "%04d", increasingNumber.get()-1);
        newTraceId = traceID + "." + Thread.currentThread().getId() + "." + requestTime + String.format(Locale.getDefault(), "%04d", increasingNumber.get());
        sw8 = sampled + "-" +
                Utils.encodeStringToBase64(newTraceId) + "-" +
                Utils.encodeStringToBase64(parentTraceID) + "-0-" +
                Utils.encodeStringToBase64(FTExceptionHandler.get().getTrackServiceName()) + "-" +
                Utils.encodeStringToBase64(UUID.randomUUID().toString().replace("-", "").toLowerCase() + "@" + NetUtils.get().getMobileIpAddress()) + "-" +
                Utils.encodeStringToBase64(url.encodedPath()) + "-" +
                Utils.encodeStringToBase64(url.host() + ":" + url.port());
    }
}
