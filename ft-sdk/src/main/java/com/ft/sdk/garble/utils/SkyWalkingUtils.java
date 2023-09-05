package com.ft.sdk.garble.utils;

import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.garble.http.HttpUrl;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * author: huangDianHua
 * time: 2020/8/5 20:44:31
 * description: SkyWalking sw8 ，sw6 头部拼接字段
 */
public class SkyWalkingUtils {
    public enum SkyWalkingVersion {
        V2,
        V3
    }

    /**
     * 自增整型，范围 0~9999,每次自增 2
     */
    private final static AtomicInteger increasingNumber = new AtomicInteger(-1);
    /**
     * 自增整型
     */
    private final static AtomicLong increasingLong = new AtomicLong(0);
    private final static String traceIDUUID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
    private final static String parentServiceUUID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
    private String sw8;
    private String newTraceId;
    private String newParentTraceId;

    public SkyWalkingUtils(SkyWalkingVersion version, String sampled, long requestTime, HttpUrl url, FTTraceConfig config) {
        synchronized (SkyWalkingUtils.class) {//防止多线程 increasingNumber 不安顺序增加
            if (increasingNumber.get() < 9999) {
                increasingNumber.getAndAdd(2);
            } else {
                increasingNumber.set(1);
            }
            if (version == SkyWalkingVersion.V3) {
                createSw8Head(sampled, requestTime, url, config);
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

    public String getNewParentTraceId() {
        return newParentTraceId;
    }

    /**
     * 基于 skywalking 官方 v3 算法
     *
     * <a href="https://skywalking.apache.org/docs/main/v9.0.0/en/protocols/skywalking-cross-process-propagation-headers-protocol-v3/">查看官方文档</a>
     * @param sampled
     * @param requestTime
     * @param url
     * @param config
     */
    private void createSw8Head(String sampled, long requestTime, HttpUrl url, FTTraceConfig config) {
        newParentTraceId = traceIDUUID + "." + Thread.currentThread().getId() + "." + requestTime + String.format(Locale.getDefault(), "%04d", increasingNumber.get() - 1);
        newTraceId = traceIDUUID + "." + Thread.currentThread().getId() + "." + requestTime + String.format(Locale.getDefault(), "%04d", increasingNumber.get());
        sw8 = sampled + "-" +
                Utils.encodeStringToBase64(newTraceId) + "-" +
                Utils.encodeStringToBase64(newParentTraceId) + "-" +
                "0-" +
                Utils.encodeStringToBase64(Constants.DEFAULT_SERVICE_NAME + "") + "-" +
                Utils.encodeStringToBase64(parentServiceUUID + "@" + NetUtils.getMobileIpAddress()) + "-" +
                Utils.encodeStringToBase64(url.getPath()) + "-" +
                Utils.encodeStringToBase64(url.getHost() + ":" + url.getPort());
    }

    /**
     * 基于 skywalking 官方 v2 算法,官方已经舍弃了这个
     * <a href="https://github.com/yuhwb/incubator-skywalking/blob/master/docs/en/protocols/Skywalking-Cross-Process-Propagation-Headers-Protocol-v2.md">官方算法</a>
     *
     * @param sampled
     * @param requestTime
     * @param url
     */

    private void createSw6Head(String sampled, long requestTime, HttpUrl url) {
        newParentTraceId = increasingLong.get() + "." + Thread.currentThread().getId() + "." + requestTime + String.format(Locale.getDefault(), "%04d", increasingNumber.get() - 1);
        newTraceId = increasingLong.get() + "." + Thread.currentThread().getId() + "." + requestTime + String.format(Locale.getDefault(), "%04d", increasingNumber.get());
        sw8 = sampled + "-" +
                Utils.encodeStringToBase64(newTraceId) + "-" +
                Utils.encodeStringToBase64(newParentTraceId) + "-" +
                "0-" +
                increasingLong.get() + "-" + increasingLong.get() + "-" +
                Utils.encodeStringToBase64("#" + url.getHost() + ":" + url.getPort()) + "-" +
                Utils.encodeStringToBase64("-1") + "-" + Utils.encodeStringToBase64("-1");
    }
}
