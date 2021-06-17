package com.ft.sdk.garble.manager;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceBean;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Request;
import okhttp3.Response;

public final class FTNetworkPerformanceHandler {
    private final ResourceBean bean = new ResourceBean();

    public void setTransformContent(Request request, Response response, String responseBody,
                                    String sessionId,
                                    String viewId, String viewName, String viewReferrer, String actionId,
                                    String actionName, String traceId, String spanId) {
        bean.sessionId = sessionId;
        bean.viewId = viewId;
        bean.viewName = viewName;
        bean.viewReferrer = viewReferrer;
        bean.actionId = actionId;
        bean.actionName = actionName;
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
            bean.resourceType = bean.responseContentType;
            bean.resourceStatus = response.code();
        }

        bean.resourceSize = responseBody == null ? 0 : responseBody.getBytes().length;
        bean.resourceSize += responseHeaderSize;
        bean.traceId = traceId;
        bean.spanId = spanId;

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
