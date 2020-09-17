package com.ft.sdk;

import com.ft.sdk.garble.http.HttpUrl;

import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

/**
 * author: huangDianHua
 * time: 2020/9/16 11:16:20
 * description: HttpClient 拦截器
 */
public class FTHttpClientInterceptor {
    FTTraceHandler handler;
    String operationName = "";
    String endPoint = "";
    JSONObject requestContent;

    /**
     * 拦截 HttpClient 请求
     *
     * @param request
     * @param entity
     * @param context
     * @throws HttpException
     * @throws IOException
     */
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        handler = new FTTraceHandler();
        try {
            operationName = request.getMethod() + "/http";
            URI uri = request.getUri();
            endPoint = uri.getHost() + ":" + uri.getPort();
            System.out.println("host:" + uri.getHost() + "," + "path:" + uri.getPath() + ",port:" + uri.getPort());
            HttpUrl httpUrl = new HttpUrl(uri.getHost(), uri.getPath(), uri.getPort());
            HashMap<String, String> headers = handler.getTraceHeader(httpUrl);
            Iterator<String> iterator = headers.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                request.addHeader(key, headers.get(key));
            }
            requestContent = buildRequestJsonContent(request, entity);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拦截 HttpClient 响应
     *
     * @param response
     * @param entity
     * @param context
     * @throws HttpException
     * @throws IOException
     */
    public void process(HttpResponse response, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        boolean isError = response == null || response.getCode() >= 400;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("requestContent", requestContent);
            jsonObject.put("responseContent", buildResponseJsonContent(response, entity, response.getReasonPhrase()));

            handler.traceDataUpload(jsonObject, operationName, endPoint, isError);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param request
     * @return
     * @throws IOException
     */
    private JSONObject buildRequestJsonContent(HttpRequest request, EntityDetails entity) {
        JSONObject json = new JSONObject();
        try {
            Header[] headerArr = request.getHeaders();
            JSONObject headers = new JSONObject();
            for (Header header : headerArr) {
                headers.put(header.getName(), header.getValue());
            }
            json.put("method", request.getMethod());
            json.put("url", request.getUri().toString());
            json.put("headers", headers);

            if (request instanceof BasicClassicHttpRequest) {
                BasicClassicHttpRequest httpRequest = (BasicClassicHttpRequest) request;
                HttpEntity httpEntity = httpRequest.getEntity();
                if (httpEntity != null) {
                    byte[] content = EntityUtils.toByteArray(httpEntity);
                    String body = new String(content);
                    json.put("body", body);

                    HttpEntity newHttpEntity = EntityBuilder.create()
                            .setContentType(ContentType.create(httpEntity.getContentType()))
                            .setContentEncoding(httpEntity.getContentEncoding())
                            .setBinary(content).build();
                    httpRequest.setEntity(newHttpEntity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * @param response
     * @param entity
     * @return
     */
    private JSONObject buildResponseJsonContent(HttpResponse response, EntityDetails entity, String error) {
        JSONObject json = new JSONObject();
        try {
            Header[] headerArr = response.getHeaders();
            JSONObject headers = new JSONObject();
            for (Header header : headerArr) {
                headers.put(header.getName(), header.getValue());
            }

            json.put("code", response.getCode());
            json.put("headers", headers);
            String body = "";
            if (response instanceof CloseableHttpResponse) {
                CloseableHttpResponse httpResponse = (CloseableHttpResponse) response;
                HttpEntity httpEntity = httpResponse.getEntity();
                byte[] content = EntityUtils.toByteArray(httpEntity);
                body = new String(content,StandardCharsets.ISO_8859_1);
                HttpEntity newHttpEntity = EntityBuilder.create()
                        .setContentEncoding(httpEntity.getContentEncoding())
                        .setBinary(content).build();
                httpResponse.setEntity(newHttpEntity);
            }

            JSONObject jbBody = null;
            JSONArray jaBody = null;
            try {
                jbBody = new JSONObject(body);
                json.put("body", jbBody);
            } catch (JSONException e) {
            }
            if (jbBody == null) {
                try {
                    jaBody = new JSONArray(body);
                    json.put("body", jaBody);
                } catch (JSONException e) {
                }
            }
            if (jaBody == null && jbBody == null) {
                json.put("body", body);
            }
            if (error != null && !error.isEmpty()) {
                json.put("error", error);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }
}
