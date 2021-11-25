package com.ft.sdk;

import com.ft.sdk.garble.http.HttpUrl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class FTTraceManager {
    private final ConcurrentHashMap<String, FTTraceHandler> handlerMap = new ConcurrentHashMap<>();


    private static class SingletonHolder {
        private static final FTTraceManager INSTANCE = new FTTraceManager();
    }

    public static FTTraceManager get() {
        return FTTraceManager.SingletonHolder.INSTANCE;
    }

    public HashMap<String, String> getTraceHeader(String key, String urlString)
            throws MalformedURLException {
        FTTraceHandler handler = new FTTraceHandler();
        URL url = new URL(urlString);
        HashMap<String, String> map = null;
        try {
            map = handler
                    .getTraceHeader(new HttpUrl(url.getHost(),
                            URLEncoder.encode(url.getPath(), "UTF-8"), url.getPort(), urlString));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        handlerMap.put(key, handler);
        return map;
    }

    FTTraceHandler getHandler(String key) {
        return handlerMap.get(key);
    }


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