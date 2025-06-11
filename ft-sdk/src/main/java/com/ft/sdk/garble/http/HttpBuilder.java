package com.ft.sdk.garble.http;

import android.net.Uri;
import android.util.Pair;

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
    private boolean isDataway = false;
    private final HashMap<String, String> headParams = new HashMap<>();
    private final HashMap<String, String> params = new HashMap<>();

    // 新增 HashMap 用于表单和文件数据
    private final HashMap<String, String> formParams = new HashMap<>();
    private final HashMap<String, Pair<String, byte[]>> fileParams = new HashMap<>();

    private boolean urlWithMsPrecision;

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

    public HttpBuilder enableUrlWithMsPrecision() {
        urlWithMsPrecision = true;
        return this;
    }

    public RequestMethod getMethod() {
        if (method == null) {
            throw new InvalidParameterException("method 未初始化");
        }
        return method;
    }

    public String getUrl() {
        Uri.Builder fullUrl = Uri.parse(getHost()).buildUpon();
        if (!Utils.isNullOrEmpty(model)) {
            fullUrl.appendPath(model);
            if (isDataway) {
                fullUrl.appendQueryParameter("token", httpConfig.getClientToken())
                        .appendQueryParameter("to_headless", "true");
            }
        }
        if (!params.isEmpty()) {
            for (String key : params.keySet()) {
                fullUrl.appendQueryParameter(key, params.get(key));
            }
        }
        if (urlWithMsPrecision) {
            fullUrl.appendQueryParameter("precision", "ms");
        }
        return fullUrl.build().toString();
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

    public HashMap<String, String> getFormParams() {
        return formParams;
    }

    public HashMap<String, Pair<String, byte[]>> getFileParams() {
        return fileParams;
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


    public HttpBuilder setHeadParams(HashMap<String, String> hashMap) {
        this.headParams.putAll(hashMap);
        return this;
    }

    public HttpBuilder addHeadParam(String key, String value) {
        this.headParams.put(key, value);
        return this;
    }

    public HttpBuilder addParam(String key, String value) {
        this.params.put(key, value);
        return this;
    }

    public HttpBuilder setFormParams(HashMap<String, String> formParams) {
        this.formParams.clear();
        this.formParams.putAll(formParams);
        return this;
    }

    public HttpBuilder addFormParam(String key, String value) {
        this.formParams.put(key, value);
        return this;
    }

    public HttpBuilder setFileParams(HashMap<String, Pair<String, byte[]>> fileParams) {
        this.fileParams.clear();
        this.fileParams.putAll(fileParams);
        return this;
    }

    public HttpBuilder addFileParam(String key, Pair<String, byte[]> file) {
        this.fileParams.put(key, file);
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
