package com.ft.sdk;

import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.bean.TraceBean;
import com.ft.sdk.garble.http.HttpUrl;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;

/**
 * author: huangDianHua
 * time: 2020/9/16 11:34:48
 * description: trace 数据处理
 */
class FTTraceHandler {
    private static final String ZIPKIN_TRACE_ID = "X-B3-TraceId";
    private static final String ZIPKIN_SPAN_ID = "X-B3-SpanId";
    private static final String ZIPKIN_SAMPLED = "X-B3-Sampled";
    private static final String JAEGER_KEY = "uber-trace-id";
    private static final String SKYWALKING_V3_SW_8 = "sw8";
    private static final String SKYWALKING_V3_SW_6 = "sw6";
    //是否可以采样
    private final boolean enableTrace;
    //请求开始时间
    private final long requestTime = Utils.getCurrentNanoTime();
    private final String traceID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
    private final String spanID = Utils.getGUID_16();
    private boolean isWebViewTrace;

    private HttpUrl httpUrl;

    public String getTraceID() {
        return traceID;
    }

    public String getSpanID() {
        return spanID;
    }

    public FTTraceHandler() {
        enableTrace = Utils.enableTraceSamplingRate();
    }

    public HashMap<String, String> getTraceHeader(HttpUrl httpUrl) {
        this.httpUrl = httpUrl;
        HashMap<String, String> headers = new HashMap<>();
        String sampled;
        //抓取数据内容
        if (enableTrace) {
            sampled = "1";
        } else {
            sampled = "0";
        }
        String parentSpanID = "0000000000000000";

        //在数据中添加标记
        if (FTHttpConfig.get().traceType == TraceType.ZIPKIN) {
            headers.put(ZIPKIN_SPAN_ID, spanID);
            headers.put(ZIPKIN_TRACE_ID, traceID);
            headers.put(ZIPKIN_SAMPLED, sampled);
        } else if (FTHttpConfig.get().traceType == TraceType.JAEGER) {
            headers.put(JAEGER_KEY, traceID + ":" + spanID + ":" + parentSpanID + ":" + sampled);
        }
//        else if (FTHttpConfig.get().traceType == TraceType.SKYWALKING_V3) {
//            SkyWalkingUtils skyWalkingUtils = new SkyWalkingUtils(SkyWalkingUtils.SkyWalkingVersion.V3, sampled, requestTime, httpUrl);
//            traceID = skyWalkingUtils.getNewTraceId();
//            spanID = skyWalkingUtils.getNewParentTraceId() + "0";
//            headers.put(SKYWALKING_V3_SW_8, skyWalkingUtils.getSw());
//        } else if (FTHttpConfig.get().traceType == TraceType.SKYWALKING_V2) {
//            SkyWalkingUtils skyWalkingUtils = new SkyWalkingUtils(SkyWalkingUtils.SkyWalkingVersion.V2, sampled, requestTime, httpUrl);
//            traceID = skyWalkingUtils.getNewTraceId();
//            spanID = skyWalkingUtils.getNewParentTraceId() + "0";
//            headers.put(SKYWALKING_V3_SW_6, skyWalkingUtils.getSw());
//        }
        return headers;
    }

    public void traceDataUpload(JSONObject content, String operationName, boolean isError) {
        if (!enableTrace) {
            return;
        }
        //请求结束时间
        long responseTime = Utils.getCurrentNanoTime();
        long duration = (responseTime - requestTime) / 1000;

        String endPoint = httpUrl.getHost() + ":" + httpUrl.getPort();

        TraceBean traceBean = new TraceBean(FTHttpConfig.get().traceType.toString(), content, requestTime);
        traceBean.setOperationName(operationName);
        traceBean.setDuration(duration);
        traceBean.setSpanType("entry");
        traceBean.setEndpoint(endPoint);
        traceBean.setStatus(isError ? "error" : "ok");
        traceBean.setServiceName(FTExceptionHandler.get().getTrackServiceName());
        traceBean.setSpanID(spanID);
        traceBean.setTraceID(traceID);
        FTTrackInner.getInstance().traceBackground(traceBean);
    }


    public void setIsWebViewTrace(boolean isWebViewTrace) {
        this.isWebViewTrace = isWebViewTrace;
    }
}
