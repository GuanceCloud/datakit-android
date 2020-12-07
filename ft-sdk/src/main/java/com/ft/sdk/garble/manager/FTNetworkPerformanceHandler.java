package com.ft.sdk.garble.manager;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceBean;

import java.net.InetAddress;

import okhttp3.Connection;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public final class FTNetworkPerformanceHandler {
    private final ResourceBean bean = new ResourceBean();

    public void setTransformContent(Request request, Response response, String responseBody, Connection connection) {
        bean.url = request.url().toString();
        bean.urlHost = request.url().host();
        bean.urlPath = request.url().encodedPath();
        bean.requestHeader = request.headers().toString();
        if (response != null) {
            bean.responseHeader = response.headers().toString();
            bean.responseContentType = response.header("Content-Type");
            bean.responseConnection = response.header("Connection");
            bean.resourceMethod = request.method();
            bean.responseContentEncoding = response.header("Content-Encoding");
            bean.resourceStatus = response.code();
        }

        bean.resourceSize = responseBody == null ? 0 : responseBody.length();

        if (connection != null) {
            Route route = connection.route();
            InetAddress address = route.socketAddress().getAddress();
            if (address != null) {
                bean.responseServer = route.socketAddress().getAddress().getHostAddress();
            }

        }

    }

    public void setTransformPerformance(NetStatusBean netStatusBean) {
        bean.resourceDNS = netStatusBean.getDNSTime();
        bean.resourceSSL = netStatusBean.getSSLTime();
        bean.resourceTCP = netStatusBean.getTcpTime();

        bean.resourceTrans = netStatusBean.getResponseTime();
        bean.resourceTTFB = netStatusBean.getTTFB();
        bean.resourceLoad = netStatusBean.getHoleRequestTime();

    }

    public void handleUpload() {
        FTAutoTrack.putResourcePerformance(bean);
        bean.reset();
    }


}
