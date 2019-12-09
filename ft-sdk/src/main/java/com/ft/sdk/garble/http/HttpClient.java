package com.ft.sdk.garble.http;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSDKInstall;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

/**
 * BY huangDianHua
 * DATE:2019-11-29 18:40
 * Description:
 */
public abstract class HttpClient {
    protected final int SEND_OUT_TIME = 30*1000;
    protected final int READ_OUT_TIME = 10*1000;
    protected final String CHARSET = "UTF-8";
    protected final String CONTENT_TYPE = "text/plain";
    protected HttpBuilder mHttpBuilder;
    protected HttpURLConnection mConnection;
    protected boolean connSuccess = false;
    protected FTHttpConfig ftHttpConfig = FTHttpConfig.get();
    public static HttpBuilder Builder(){
        return new HttpBuilder();
    }
    protected abstract String getBodyContent();
    public HttpClient(HttpBuilder httpBuilder){
        this.mHttpBuilder = httpBuilder;
        if(openConnection()) {
            setCommonParams();
            setHeadParams();
        }
    }

    protected boolean openConnection(){
        try{
            String urlStr = ftHttpConfig.metricsUrl;
            if(mHttpBuilder.getUrl() != null){
                urlStr = mHttpBuilder.getUrl();
            }
            String model = mHttpBuilder.getModel();
            if(model != null && !model.isEmpty()){
                urlStr = urlStr + "/" +model;
            }
            final URL url = new URL(urlStr+getQueryString());
            mConnection = (HttpURLConnection) url.openConnection();
            if(mConnection == null){
                LogUtils.e(String.format("connect %s feature",url.toString()));
            }else{
                connSuccess = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return connSuccess;
    }
    private String getQueryString(){
        StringBuffer sb = new StringBuffer();
        if (mHttpBuilder.getMethod() == RequestMethod.GET) {
            HashMap<String,Object> param = mHttpBuilder.getParams();
            if (param != null) {
                Iterator<String> keys = param.keySet().iterator();
                while (keys.hasNext()){
                    String key = keys.next();
                    sb.append("&"+key+"="+param.get(key));
                }
            }
        }
        return sb.toString();
    }

    private void setCommonParams(){
        if(mHttpBuilder.getSendOutTime() == 0){
            mHttpBuilder.setSendOutTime(SEND_OUT_TIME);
        }
        if(mHttpBuilder.getReadOutTime() == 0){
            mHttpBuilder.setReadOutTime(READ_OUT_TIME);
        }

        try {
            mConnection.setRequestMethod(mHttpBuilder.getMethod().method);
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        mConnection.setConnectTimeout(mHttpBuilder.getSendOutTime());
        mConnection.setReadTimeout(mHttpBuilder.getReadOutTime());
    }

    private void setHeadParams(){
        mConnection.addRequestProperty("X-Datakit-UUID",ftHttpConfig.uuid);
        mConnection.addRequestProperty("User-Agent",ftHttpConfig.userAgent);
        mConnection.addRequestProperty("Accept-Language","zh-CN");
        mConnection.addRequestProperty("Content-Type",CONTENT_TYPE+";charset="+CHARSET);
        HashMap<String,String> headMap =  mHttpBuilder.getHeadParams();
        Iterator<String> keys = headMap.keySet().iterator();
        while (keys.hasNext()){
            String key = keys.next();
            mConnection.addRequestProperty(key,headMap.get(key));
        }
    }

    public void execute(final HttpCallback httpCallback){
        request(httpCallback);
    }

    private void request(HttpCallback httpCallback){
        RequestMethod method = mHttpBuilder.getMethod();
        boolean isDoInput = method == RequestMethod.POST;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        String tempLine = null;
        StringBuffer resultBuffer = new StringBuffer();
        if(isDoInput){
            mConnection.setDoOutput(true);
        }
        mConnection.setDoInput(true);
        mConnection.setUseCaches(false);
        try {
            mConnection.connect();
            if(isDoInput){
                outputStream = mConnection.getOutputStream();
                outputStream.write(getBodyContent().getBytes(CHARSET));
                outputStream.flush();
            }
            int responseCode = mConnection.getResponseCode();
            if(responseCode >= 300){
                inputStream = mConnection.getErrorStream();
                inputStreamReader = new InputStreamReader(inputStream,CHARSET);
                reader = new BufferedReader(inputStreamReader);
                while ((tempLine = reader.readLine()) != null){
                    resultBuffer.append(tempLine);
                }
            }else if(responseCode == HttpURLConnection.HTTP_OK){
                inputStream = mConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream,CHARSET);
                reader = new BufferedReader(inputStreamReader);
                while ((tempLine = reader.readLine())!=null){
                    resultBuffer.append(tempLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(inputStreamReader != null) {
                    inputStreamReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        httpCallback.onComplete(resultBuffer.toString());
    }
}
