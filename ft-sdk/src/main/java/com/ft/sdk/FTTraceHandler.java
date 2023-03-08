package com.ft.sdk;

import com.ft.sdk.garble.http.HttpUrl;
import com.ft.sdk.garble.utils.SkyWalkingUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;
import java.util.UUID;

/**
 * author: huangDianHua
 * time: 2020/9/16 11:34:48
 * description: trace 数据处理
 */
public class FTTraceHandler {
    public static final String ZIPKIN_TRACE_ID = "X-B3-TraceId";
    public static final String ZIPKIN_SPAN_ID = "X-B3-SpanId";
    public static final String ZIPKIN_SAMPLED = "X-B3-Sampled";
    public static final String ZIPKIN_B3_HEADER = "b3";
    public static final String W3C_TRACEPARENT_KEY = "traceparent";
    public static final String JAEGER_KEY = "uber-trace-id";
    public static final String SKYWALKING_V3_SW_8 = "sw8";
//    public static final String SKYWALKING_V3_SW_6 = "sw6";

    public static final String DD_TRACE_TRACE_ID_KEY = "x-datadog-trace-id";

    public static final String DD_TRACE_PARENT_SPAN_ID_KEY = "x-datadog-parent-id";
    public static final String DD_TRACE_SAMPLING_PRIORITY_KEY = "x-datadog-sampling-priority";
    public static final String DD_TRACE_ORIGIN_KEY = "x-datadog-origin";
    /**
     * 是否可以采样
     */
    private final boolean enableTrace;
    /**
     * 请求开始时间
     */
    private final long requestTime = Utils.getCurrentNanoTime();
    private String traceID = "";
    private String spanID = "";

    private HttpUrl httpUrl;
    private final FTTraceConfig config;

    public String getTraceID() {
        return traceID;
    }

    public String getSpanID() {
        return spanID;
    }

    public HttpUrl getUrl() {
        return httpUrl;
    }

    FTTraceHandler() {
        config = FTTraceConfigManager.get().getConfig();
        enableTrace = config != null && Utils.enableTraceSamplingRate(config.getSamplingRate());

    }

    HashMap<String, String> getTraceHeader(HttpUrl httpUrl) {

        if (config == null) return new HashMap<>();
        this.httpUrl = httpUrl;
        HashMap<String, String> headers = new HashMap<>();
        String sampled;
        //抓取数据内容
        if (enableTrace) {
            sampled = "1";
        } else {
            sampled = "0";
        }

        //在数据中添加标记
        if (config.getTraceType() == TraceType.ZIPKIN_MULTI_HEADER
                || config.getTraceType() == TraceType.ZIPKIN_SINGLE_HEADER
                || config.getTraceType() == TraceType.JAEGER || config.getTraceType() == TraceType.TRACEPARENT) {
            traceID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
            spanID = Utils.getGUID_16();
        } else if (config.getTraceType() == TraceType.DDTRACE) {
            traceID = Utils.getDDtraceNewId() + "";
            spanID = Utils.getDDtraceNewId() + "";
        }

        if (config.getTraceType() == TraceType.ZIPKIN_MULTI_HEADER) {
            headers.put(ZIPKIN_SPAN_ID, spanID);
            headers.put(ZIPKIN_TRACE_ID, traceID);
            headers.put(ZIPKIN_SAMPLED, sampled);
        } else if (config.getTraceType() == TraceType.ZIPKIN_SINGLE_HEADER) {
            headers.put(ZIPKIN_B3_HEADER, traceID + "-" + spanID + "-" + sampled);
        } else if (config.getTraceType() == TraceType.TRACEPARENT) {
            String version = "00";
            String sampledStr = "0" + sampled;
            String parentID = spanID;
            headers.put(W3C_TRACEPARENT_KEY, version + "-" + traceID + "-" + parentID + "-" + sampledStr);
        } else if (config.getTraceType() == TraceType.JAEGER) {
            String parentSpanID = "0";
            headers.put(JAEGER_KEY, traceID + ":" + spanID + ":" + parentSpanID + ":" + sampled);
        } else if (config.getTraceType() == TraceType.DDTRACE) {
            traceID = Utils.getDDtraceNewId() + "";
            spanID = Utils.getDDtraceNewId() + "";
            headers.put(DD_TRACE_ORIGIN_KEY, "rum");
            headers.put(DD_TRACE_SAMPLING_PRIORITY_KEY, enableTrace ? "2" : "-1");
            headers.put(DD_TRACE_PARENT_SPAN_ID_KEY, spanID);
            headers.put(DD_TRACE_TRACE_ID_KEY, traceID);
        } else if (config.getTraceType() == TraceType.SKYWALKING) {
            SkyWalkingUtils skyWalkingUtils = new SkyWalkingUtils(SkyWalkingUtils.SkyWalkingVersion.V3, sampled, requestTime, httpUrl, config);
            traceID = skyWalkingUtils.getNewTraceId();
            spanID = skyWalkingUtils.getNewParentTraceId() + "0";
            headers.put(SKYWALKING_V3_SW_8, skyWalkingUtils.getSw());
        }
//        else if (FTHttpConfig.get().traceType == TraceType.SKYWALKING_V2) {
//            SkyWalkingUtils skyWalkingUtils = new SkyWalkingUtils(SkyWalkingUtils.SkyWalkingVersion.V2, sampled, requestTime, httpUrl);
//            traceID = skyWalkingUtils.getNewTraceId();
//            spanID = skyWalkingUtils.getNewParentTraceId() + "0";
//            headers.put(SKYWALKING_V3_SW_6, skyWalkingUtils.getSw());
//        }
        return headers;
    }

//    void traceDataUpload(JSONObject content, String operationName, boolean isError) {
//        if (!enableTrace) {
//            return;
//        }
//        //请求结束时间
//        long responseTime = Utils.getCurrentNanoTime();
//        long duration = (responseTime - requestTime) / 1000;
//
//        String endPoint = httpUrl.getHost() + ":" + httpUrl.getPort();
//
//        TraceBean traceBean = new TraceBean(config.getTraceType().toString(), content, requestTime);
//        traceBean.setOperationName(operationName);
//        traceBean.setDuration(duration);
//        traceBean.setSpanType("entry");
//        traceBean.setEndpoint(endPoint);
//        traceBean.setStatus(isError ? "error" : "ok");
//        traceBean.setServiceName(config.getServiceName());
//        traceBean.setSpanID(spanID);
//        traceBean.setTraceID(traceID);
//        FTTrackInner.getInstance().traceBackground(traceBean);
//    }


}
