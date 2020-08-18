package com.ft.sdk.garble.utils;

import com.ft.sdk.garble.FTExceptionHandler;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.HttpUrl;

/**
 * author: huangDianHua
 * time: 2020/8/5 20:44:31
 * description: SkyWalking sw8 头部拼接字段
 */
public class SkyWalkingUtils {
    public enum SkyWalkingVersion {
        V2, V3
    }

    private static AtomicInteger increasingNumber = new AtomicInteger(-1);
    private static AtomicLong increasingLong = new AtomicLong(0);
    private static String traceIDUUID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
    private static String parentServiceUUID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
    private String sw8;
    private String newTraceId;
    private String newSpanId;

    public SkyWalkingUtils(SkyWalkingVersion version, String sampled, long requestTime, HttpUrl url) {
        synchronized (SkyWalkingUtils.class) {//防止多线程 increasingNumber 不安顺序增加
            if (increasingNumber.get() < 9999) {
                increasingNumber.getAndAdd(2);
            } else {
                increasingNumber.set(1);
            }
            if (version == SkyWalkingVersion.V3) {
                createSw8Head(sampled, requestTime, url);
            } else if (version == SkyWalkingVersion.V2) {
                increasingLong.getAndIncrement();
                createSw6Head(sampled, requestTime, url);
            }
        }
    }

    public String getSw() {
        return sw8;
    }

    public String getNewTraceId() {
        return newTraceId;
    }

    private void createSw8Head(String sampled, long requestTime, HttpUrl url) {
        newSpanId = traceIDUUID + "." + Thread.currentThread().getId() + "." + requestTime + String.format(Locale.getDefault(), "%04d", increasingNumber.get() - 1);
        newTraceId = traceIDUUID + "." + Thread.currentThread().getId() + "." + requestTime + String.format(Locale.getDefault(), "%04d", increasingNumber.get());
        sw8 = sampled + "-" +
                Utils.encodeStringToBase64(newTraceId) + "-" +
                Utils.encodeStringToBase64(newSpanId) + "-0-" +
                Utils.encodeStringToBase64(FTExceptionHandler.get().getTrackServiceName()) + "-" +
                Utils.encodeStringToBase64(parentServiceUUID + "@" + NetUtils.get().getMobileIpAddress()) + "-" +
                Utils.encodeStringToBase64(url.encodedPath()) + "-" +
                Utils.encodeStringToBase64(url.host() + ":" + url.port());
    }

    private void createSw6Head(String sampled, long requestTime, HttpUrl url) {
        newSpanId = increasingLong.get() + "." + Thread.currentThread().getId() + "." + requestTime + String.format(Locale.getDefault(), "%04d", increasingNumber.get() - 1);
        newTraceId = increasingLong.get() + "." + Thread.currentThread().getId() + "." + requestTime + String.format(Locale.getDefault(), "%04d", increasingNumber.get());
        sw8 = sampled + "-" +
                Utils.encodeStringToBase64(newTraceId) + "-" +
                Utils.encodeStringToBase64(newSpanId) + "-0-" +
                increasingLong.get() + "-" + increasingLong.get() + "-" +
                Utils.encodeStringToBase64("#" + url.host() + ":" + url.port()) +"-"+
                Utils.encodeStringToBase64("-1") + "-" + Utils.encodeStringToBase64("-1");
    }
}
