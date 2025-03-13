package com.ft.sdk.garble.http;

import com.ft.sdk.NetProxy;
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

    /**
     * dataway 数据收发地址
     */
    private final static String DATAWAY_URL_HOST_FORMAT = "/%s?token=%s&to_headless=true";
    private String host;
    private String model;
    private RequestMethod method;
    private String bodyString;
    private final FTHttpConfigManager httpConfig;

    /**
     * {@link FTHttpConfigManager#sendOutTime}
     */
    private final int sendOutTime;
    /**
     * {@link FTHttpConfigManager#readOutTime}
     */
    private final int readOutTime;
    private boolean useDefaultHead = true;
    private boolean isDataway = false;
    private final HashMap<String, String> headParams = new HashMap<>();

    public static HttpBuilder Builder() {
        return new HttpBuilder(FTHttpConfigManager.get());
    }

    public HttpBuilder(FTHttpConfigManager httpConfig) {
        this.httpConfig = httpConfig;
        this.sendOutTime = httpConfig.getSendOutTime();
        this.readOutTime = httpConfig.getReadOutTime();
    }

    public String getHost() {
        if (host == null) {
            if (!Utils.isNullOrEmpty(httpConfig.getDatakitUrl())) {
                host = httpConfig.getDatakitUrl();
            } else {
                isDataway = true;
                host = httpConfig.getDatawayUrl();
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

    public String getUrlWithMsPrecision() {
        return getUrl() + (isDataway ? "&" : "?") + "precision=ms";
    }

    public String getUrl() {
        String url = getHost();
        if (!Utils.isNullOrEmpty(model)) {
            if (!isDataway) {
                url += "/" + model;
            } else {
                url += String.format(DATAWAY_URL_HOST_FORMAT, model, httpConfig.getClientToken());
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

    public FTHttpConfigManager getHttpConfig() {
        return httpConfig;
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
