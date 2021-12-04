package com.ft.sdk;

import com.ft.sdk.garble.http.HttpUrl;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 链路追踪
 */
public class FTTraceManager {
    private final ConcurrentHashMap<String, FTTraceHandler> handlerMap = new ConcurrentHashMap<>();


    private static class SingletonHolder {
        private static final FTTraceManager INSTANCE = new FTTraceManager();
    }

    public static FTTraceManager get() {
        return FTTraceManager.SingletonHolder.INSTANCE;
    }

    /**
     * 获取 trace http 请求头参数
     * @param key 链路 id
     * @param urlString url 地址
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public HashMap<String, String> getTraceHeader(String key, String urlString)
            throws MalformedURLException, URISyntaxException {
        FTTraceHandler handler = new FTTraceHandler();

        URL url = Utils.parseFromUrl(urlString);
        HashMap<String, String> map = handler
                .getTraceHeader(new HttpUrl(url.getHost(), url.getPath(), url.getPort(), urlString));

        handlerMap.put(key, handler);
        return map;
    }

    FTTraceHandler getHandler(String key) {
        return handlerMap.get(key);
    }


    /**
     * 发送 trace 数据
     * @param key  链路 id
     * @param httpMethod 请求类型 post ，get
     * @param requestHeader  请求数据头
     * @param responseHeader 数据响应头
     * @param statusCode http 状态吗
     * @param errorMsg 请求错误信息
     */
    public void addTrace(String key, String httpMethod, HashMap<String, String> requestHeader,
                         HashMap<String, String> responseHeader, int statusCode,
                         String errorMsg) {

        FTTraceHandler handler = handlerMap.get(key);
        if (handler != null) {
            String url = handler.getUrl().getHoleUrl();
            String path = handler.getUrl().getPath();
            String operationName = httpMethod.toUpperCase() + " " + path;
            JSONObject json = new JSONObject();
            try {
                JSONObject requestContent = new JSONObject();
                requestContent.put("method", httpMethod);
                requestContent.put("headers", requestHeader);
                requestContent.put("url", url);

                JSONObject responseContent = new JSONObject();
                responseContent.put("code", statusCode);
                responseContent.put("headers", responseHeader);
                boolean isError = false;
                if (statusCode >= 400) {
                    responseContent.put("error", errorMsg);
                    isError = true;
                }

                json.put("requestContent", requestContent);
                json.put("responseContent", responseContent);

                handler.traceDataUpload(json, operationName, isError);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        handlerMap.remove(key);

    }


}
