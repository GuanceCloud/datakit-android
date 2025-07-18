package com.ft.sdk.garble.utils;

import java.net.URL;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * author: huangDianHua
 * time: 2020/8/5 20:44:31
 * description: SkyWalking sw8, sw6 header concatenation fields
 */
public class SkyWalkingUtils {
    public enum SkyWalkingVersion {
        V2,
        V3
    }

    /**
     * Auto-increment integer, range 0~9999, increments by 2 each time
     */
    private final static AtomicInteger increasingNumber = new AtomicInteger(-1);
    /**
     * Auto-increment integer
     */
    private final static AtomicLong increasingLong = new AtomicLong(0);
    private final static String traceIDUUID = Utils.randomUUID().toLowerCase();
    private final static String parentServiceUUID = Utils.randomUUID().toLowerCase();
    private String sw8;
    private String newTraceId;
    private String newParentTraceId;

    public SkyWalkingUtils(SkyWalkingVersion version, String sampled, long requestTime, URL url, String serviceName) {
        synchronized (SkyWalkingUtils.class) {// Prevent multi-thread increasingNumber from not increasing in order
            if (increasingNumber.get() < 9999) {
                increasingNumber.getAndAdd(2);
            } else {
                increasingNumber.set(1);
            }
            if (version == SkyWalkingVersion.V3) {
                createSw8Head(sampled, requestTime, url, serviceName);
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
     * Based on SkyWalking official v3 algorithm
     *
     * <a href="https://skywalking.apache.org/docs/main/v9.0.0/en/protocols/skywalking-cross-process-propagation-headers-protocol-v3/">View official documentation</a>
     *
     * @param sampled
     * @param requestTime
     * @param url
     * @param serviceName
     */
    private void createSw8Head(String sampled, long requestTime, URL url, String serviceName) {
        newParentTraceId = traceIDUUID + "." + Thread.currentThread().getId() + "." + requestTime + String.format(Locale.getDefault(), "%04d", increasingNumber.get() - 1);
        newTraceId = traceIDUUID + "." + Thread.currentThread().getId() + "." + requestTime + String.format(Locale.getDefault(), "%04d", increasingNumber.get());
        sw8 = sampled + "-" +
                Utils.encodeStringToBase64(newTraceId) + "-" +
                Utils.encodeStringToBase64(newParentTraceId) + "-" +
                "0-" +
                Utils.encodeStringToBase64(serviceName) + "-" +
                Utils.encodeStringToBase64(parentServiceUUID + "@" + NetUtils.getMobileIpAddress()) + "-" +
                Utils.encodeStringToBase64(url.getPath()) + "-" +
                Utils.encodeStringToBase64(url.getHost() + ":" + url.getPort());
    }

    /**
     * Based on SkyWalking official v2 algorithm, official has abandoned this
     * <a href="https://github.com/yuhwb/incubator-skywalking/blob/master/docs/en/protocols/Skywalking-Cross-Process-Propagation-Headers-Protocol-v2.md">Official algorithm</a>
     *
     * @param sampled
     * @param requestTime
     * @param url
     */

    private void createSw6Head(String sampled, long requestTime, URL url) {
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
