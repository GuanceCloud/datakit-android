package com.ft.sdk;

import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceBean;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Request;
import okhttp3.Response;

public final class FTNetworkRumHandler {
    private final ResourceBean bean = new ResourceBean();

    private final boolean rumEnable = FTRUMConfigManager.get().isRumEnable();
    private final boolean rumTraceRelative = FTTraceConfigManager.get().isEnableLinkRUMData();

    void startResource() {
        if (!rumEnable) return;
        bean.sessionId = FTRUMGlobalManager.get().getSessionId();
        bean.viewId = FTRUMGlobalManager.get().getViewId();
        bean.viewName = FTRUMGlobalManager.get().getViewName();
        bean.viewReferrer = FTRUMGlobalManager.get().getViewReferrer();
        bean.actionId = FTRUMGlobalManager.get().getActionId();
        bean.actionName = FTRUMGlobalManager.get().getActionName();

        FTRUMGlobalManager.get().startResource(bean.viewId, bean.actionId);

    }

    void stopResource() {
        if (!rumEnable) return;
        FTRUMGlobalManager.get().stopResource(bean.viewId, bean.actionId);
    }


    public void setTransformContent(Request request, Response response, String responseBody,
                                    String traceId, String spanId) {

        bean.url = request.url().toString();
        bean.urlHost = request.url().host();
        bean.urlPath = request.url().encodedPath();
        bean.requestHeader = request.headers().toString();
        bean.resourceUrlQuery = request.url().query();
        int responseHeaderSize = 0;
        if (response != null) {
            bean.responseHeader = response.headers().toString();
            responseHeaderSize = bean.responseHeader.getBytes().length;
            bean.responseContentType = response.header("Content-Type");
            bean.responseConnection = response.header("Connection");
            bean.resourceMethod = request.method();
            bean.responseContentEncoding = response.header("Content-Encoding");
            bean.resourceType = "xhr";
            bean.resourceStatus = response.code();
        }

        bean.resourceSize = responseBody == null ? 0 : responseBody.getBytes().length;
        bean.resourceSize += responseHeaderSize;
        if (rumTraceRelative) {
            bean.traceId = traceId;
            bean.spanId = spanId;
        }
    }

    public void setTransformPerformance(NetStatusBean netStatusBean) {
        bean.resourceDNS = netStatusBean.getDNSTime();
        bean.resourceSSL = netStatusBean.getSSLTime();
        bean.resourceTCP = netStatusBean.getTcpTime();

        bean.resourceTrans = netStatusBean.getResponseTime();
        bean.resourceTTFB = netStatusBean.getTTFB();
        bean.resourceLoad = netStatusBean.getHoleRequestTime();
        bean.resourceFirstByte = netStatusBean.getFirstByteTime();

    }

    public void handleUpload() {
        if (bean.resourceStatus >= HttpsURLConnection.HTTP_OK) {
            FTAutoTrack.putRUMResourcePerformance(bean);
        }
        bean.reset();
    }


}
