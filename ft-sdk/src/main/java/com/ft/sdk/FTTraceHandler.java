package com.ft.sdk;

import com.ft.sdk.garble.FTExceptionHandler;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.FTTrackInner;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.http.HttpUrl;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.SkyWalkingUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
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
    public static final String JAEGER_KEY = "uber-trace-id";
    public static final String SKYWALKING_V3_SW_8 = "sw8";
    public static final String SKYWALKING_V3_SW_6 = "sw6";
    //是否可以采样
    private boolean enableTrace;
    //请求开始时间
    private long requestTime = System.currentTimeMillis();
    private String traceID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
    private String spanID = Utils.getGUID_16();
    private boolean isWebViewTrace;
    private HttpUrl httpUrl;

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
        } else if (FTHttpConfig.get().traceType == TraceType.SKYWALKING_V3) {
            SkyWalkingUtils skyWalkingUtils = new SkyWalkingUtils(SkyWalkingUtils.SkyWalkingVersion.V3, sampled, requestTime, httpUrl);
            traceID = skyWalkingUtils.getNewTraceId();
            spanID = skyWalkingUtils.getNewParentTraceId() + "0";
            headers.put(SKYWALKING_V3_SW_8, skyWalkingUtils.getSw());
        } else if (FTHttpConfig.get().traceType == TraceType.SKYWALKING_V2) {
            SkyWalkingUtils skyWalkingUtils = new SkyWalkingUtils(SkyWalkingUtils.SkyWalkingVersion.V2, sampled, requestTime, httpUrl);
            traceID = skyWalkingUtils.getNewTraceId();
            spanID = skyWalkingUtils.getNewParentTraceId() + "0";
            headers.put(SKYWALKING_V3_SW_6, skyWalkingUtils.getSw());
        }
        return headers;
    }

    public void traceDataUpload(JSONObject content, String operationName, boolean isError) {
        if (!enableTrace) {
            return;
        }
        //请求结束时间
        long responseTime = System.currentTimeMillis();
        long duration = (responseTime - requestTime) * 1000;

        String endPoint = httpUrl.getHost() + ":" + httpUrl.getPort();

        LogBean logBean = new LogBean(Constants.FT_LOG_DEFAULT_MEASUREMENT, content, requestTime);
        logBean.setOperationName(operationName);
        logBean.setDuration(duration);
        logBean.setClazz("tracing");
        logBean.setSpanType("entry");
        logBean.setEndpoint(endPoint);
        logBean.setIsError(String.valueOf(isError));
        logBean.setServiceName(FTExceptionHandler.get().getTrackServiceName());
        logBean.setSpanID(spanID);
        logBean.setTraceID(traceID);
        FTTrackInner.getInstance().logBackground(logBean);


        OP op = isWebViewTrace ? OP.HTTP_WEBVIEW : OP.HTTP_CLIENT;
        FTAutoTrack.putHttpError(requestTime, op, httpUrl.getHoleUrl(), httpUrl.getHost(), isError, duration);
    }



    public void setIsWebViewTrace(boolean isWebViewTrace) {
        this.isWebViewTrace = isWebViewTrace;
    }
}
