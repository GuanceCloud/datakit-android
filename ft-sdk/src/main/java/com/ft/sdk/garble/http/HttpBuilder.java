package com.ft.sdk.garble.http;

import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.utils.Utils;

import java.security.InvalidParameterException;
import java.util.HashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-06 19:50
 * Description:
 */
public class HttpBuilder {

    private final static String DATAWAY_URL_HOST_FORMAT = "/%s?token=%s&to_headless=true";
    private String host;
    private String model;
    private RequestMethod method;
    private String bodyString;
    private int sendOutTime = FTHttpConfigManager.get().sendOutTime;
    private int readOutTime = FTHttpConfigManager.get().readOutTime;
    private boolean useDefaultHead = true;
    private boolean isDataway = false;
    private final HashMap<String, String> headParams = new HashMap<>();

    public static HttpBuilder Builder() {
        return new HttpBuilder();
    }

    public String getHost() {
        if (host == null) {
            if (!Utils.isNullOrEmpty(FTHttpConfigManager.get().datakitUrl)) {
                host = FTHttpConfigManager.get().datakitUrl;
            } else {
                isDataway = true;
                host = FTHttpConfigManager.get().datawayUrl;
            }

        }
        return host;
    }

    public RequestMethod getMethod() {
        if (method == null) {
            throw new InvalidParameterException("method 未初始化");
        }
        return method;
    }

    public String getUrl() {
        String url = getHost();
        if (!Utils.isNullOrEmpty(model)) {
            if (!isDataway) {
                url += "/" + model;
            } else {
                url += String.format(DATAWAY_URL_HOST_FORMAT, model, FTHttpConfigManager.get().clientToken);
            }
        }
        return url;
    }

    public String getBodyString() {
        return bodyString;
    }

    public String getModel() {
        return model;
    }

    public int getSendOutTime() {
        return sendOutTime;
    }

    public int getReadOutTime() {
        return readOutTime;
    }

    public HashMap<String, String> getHeadParams() {
        return headParams;
    }

    public boolean isUseDefaultHead() {
        return useDefaultHead;
    }

    public HttpBuilder setUrl(String host) {
        this.host = host;
        return this;
    }

    public HttpBuilder setMethod(RequestMethod method) {
        this.method = method;
        return this;
    }

    public HttpBuilder setBodyString(String bodyString) {
        this.bodyString = bodyString;
        return this;
    }

    public HttpBuilder setSendOutTime(int time) {
        this.sendOutTime = time;
        FTHttpConfigManager.get().sendOutTime = time;
        return this;
    }

    public HttpBuilder setReadOutTime(int time) {
        this.readOutTime = time;
        FTHttpConfigManager.get().readOutTime = time;
        return this;
    }

    public HttpBuilder setModel(String model) {
        this.model = model;
        return this;
    }


    public HttpBuilder useDefaultHead(boolean useDefault) {
        this.useDefaultHead = useDefault;
        return this;
    }

    public HttpBuilder setHeadParams(HashMap<String, String> hashMap) {
        this.headParams.putAll(hashMap);
        return this;
    }

    public HttpBuilder addHeadParam(String key, String value) {
        this.headParams.put(key, value);
        return this;
    }

    /**
     * 数据同步 HTTP 请求
     *
     * @return
     */
    public FTResponseData executeSync() {
        return new NetProxy(this).execute();
    }
}
