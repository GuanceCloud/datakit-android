package com.ft.sdk.garble.http;

import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
import com.ft.sdk.garble.utils.Utils;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * BY huangDianHua
 * DATE:2019-12-06 19:50
 * Description:
 */
public class HttpBuilder {
    private String host;
    private String model;
    private RequestMethod method;
    private String bodyString;
    private HashMap<String, Object> params = new HashMap<>();
    private int sendOutTime = FTHttpConfig.get().sendOutTime;
    private int readOutTime = FTHttpConfig.get().readOutTime;
    private boolean useDefaultHead = true;
    private boolean showLog = true;
    private HashMap<String, String> headParams = new HashMap<>();
    private boolean enableToken = true;
    public static HttpBuilder Builder() {
        return new HttpBuilder();
    }

    public String getHost() {
        if (host == null) {
            host = FTHttpConfig.get().serverUrl;
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
            url += "/" + model;
        }
        String query = getQueryString();
        if (!Utils.isNullOrEmpty(query)) {
            if(url.contains("?")){
                url+=query;
            }else {
                url += "?" + query;
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

    public HashMap<String, Object> getParams() {
        return params;
    }

    public boolean isShowLog() {
        return showLog;
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
        FTHttpConfig.get().sendOutTime = time;
        return this;
    }

    public HttpBuilder setReadOutTime(int time) {
        this.readOutTime = time;
        FTHttpConfig.get().readOutTime = time;
        return this;
    }

    public HttpBuilder setModel(String model) {
        this.model = model;
        return this;
    }

    public HttpBuilder setParams(HashMap<String, Object> hashMap) {
        this.params.putAll(hashMap);
        return this;
    }

    public HttpBuilder addParams(String key, Object value) {
        this.params.put(key, value);
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

    public HttpBuilder setShowLog(boolean show) {
        this.showLog = show;
        return this;
    }

    public HttpBuilder enableToken(boolean enableToken){
        this.enableToken = enableToken;
        return this;
    }

    public <T extends ResponseData> T executeSync(Class<T> tClass) {
        return new NetProxy(this).execute(tClass);
    }

    public <T extends ResponseData> void executeAsync(Class<T> tClass, HttpCallback<T> callback) {
        ThreadPoolUtils.get().execute(() -> callback.onComplete(new NetProxy(this).execute(tClass)));
    }

    /**
     * 封装 get 请求参数
     *
     * @return
     */
    private String getQueryString() {
        StringBuffer sb = new StringBuffer();
        //if (method == RequestMethod.GET) {
            HashMap<String, Object> param = params;
//            if(FTHttpConfig.get().dataWayToken != null && enableToken){
//                param.put("token",FTHttpConfig.get().dataWayToken);
//            }
            if (param != null) {
                Iterator<String> keys = param.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    sb.append("&" + key + "=" + param.get(key));
                }
            }
        //}
        return sb.toString();
    }
}
