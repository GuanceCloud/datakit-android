package com.ft.sdk.garble.http;

import java.security.InvalidParameterException;
import java.util.HashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-06 19:50
 * Description:
 */
public class HttpBuilder {
    private String url;
    private String model;
    private RequestMethod method;
    private String bodyString;
    private HashMap<String,Object> params = new HashMap<>();
    private int sendOutTime;
    private int readOutTime;
    private HashMap<String,String> headParams = new HashMap<>();


    public String getUrl() {
        return url;
    }

    public RequestMethod getMethod() {
        if(method == null){
            throw new InvalidParameterException("method 未初始化");
        }
        return method;
    }

    public String getBodyString(){
        return bodyString;
    }

    public String getModel(){
        return model;
    }

    public int getSendOutTime(){
        return sendOutTime;
    }

    public int getReadOutTime(){
        return readOutTime;
    }

    public HashMap<String,String> getHeadParams(){
        return headParams;
    }

    public HashMap<String,Object> getParams(){
        return params;
    }

    public HttpBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public HttpBuilder setMethod(RequestMethod method) {
        this.method = method;
        return this;
    }

    public HttpBuilder setBodyString(String bodyString){
        this.bodyString = bodyString;
        return this;
    }

    public HttpBuilder setSendOutTime(int time){
        this.sendOutTime = time;
        return this;
    }

    public HttpBuilder setReadOutTime(int time){
        this.readOutTime = time;
        return this;
    }

    public HttpBuilder setModel(String model){
        this.model = model;
        return this;
    }

    public HttpBuilder setParams(HashMap<String,Object> hashMap){
        this.params.putAll(hashMap);
        return this;
    }

    public HttpBuilder addParams(String key,Object value){
        this.params.put(key, value);
        return this;
    }

    public HttpBuilder setHeadParams(HashMap<String,String> hashMap){
        this.headParams.putAll(hashMap);
        return this;
    }

    public HttpBuilder addHeadParam(String key,String value){
        this.headParams.put(key,value);
        return this;
    }


    public HttpClient executor(){
        return new FTHttpClient(this);
    }

    public void execute(HttpCallback httpCallback){
        new FTHttpClient(this).execute(httpCallback);
    }
}
