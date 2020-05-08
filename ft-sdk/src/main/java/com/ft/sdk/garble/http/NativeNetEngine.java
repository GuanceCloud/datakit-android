package com.ft.sdk.garble.http;

import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.ft.sdk.garble.http.NetCodeStatus.UNKNOWN_EXCEPTION_CODE;


/**
 * BY huangDianHua
 * DATE:2019-11-29 18:40
 * Description: 原生 HttpUrlConnection 网络请求引擎
 */
public class NativeNetEngine implements INetEngine {
    //字符编码
    final String CHARSET = "UTF-8";
    //内容类型
    final String CONTENT_TYPE = "text/plain";
    //参数包装类
    HttpBuilder mHttpBuilder;
    //网络连接
    HttpURLConnection mConnection;
    //连接状态（true - 成功，false - 失败）
    boolean connSuccess = false;
    //网络请求的回复码
    private int responseCode = NetCodeStatus.UNKNOWN_EXCEPTION_CODE;

    @Override
    public void defaultConfig(HttpBuilder httpBuilder) {
        this.mHttpBuilder = httpBuilder;

    }

    @Override
    public void createRequest(HttpBuilder httpBuilder) {
        openConnection();
        if(connSuccess){
            setCommonParams();
            setHeadParams();
        }
    }


    /**
     * 打开连接
     * @return
     */
    private boolean openConnection() {
        try {
            final URL url = new URL(mHttpBuilder.getUrl());
            //打开连接
            mConnection = (HttpURLConnection) url.openConnection();
            if (mConnection == null) {
                //连接打开失败提示
                responseCode = NetCodeStatus.NETWORK_EXCEPTION_CODE;
                LogUtils.e(String.format("connect %s feature", url.toString()));
            } else {
                connSuccess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connSuccess;
    }

    /**
     * 设置一些公共属性
     */
    private void setCommonParams() {
        try {
            //设置连接方式
            mConnection.setRequestMethod(mHttpBuilder.getMethod().method);
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        //设置连接和读取超时时间
        mConnection.setConnectTimeout(FTHttpConfig.get().sendOutTime);
        mConnection.setReadTimeout(FTHttpConfig.get().readOutTime);
    }

    private void setHeadParams(){
        if(mHttpBuilder != null && connSuccess){
            HashMap head = mHttpBuilder.getHeadParams();
            Set<Map.Entry<String,String>> entries = head.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                mConnection.addRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public ResponseData execute() {
        try {
            return request();
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
            return getResponseData(UNKNOWN_EXCEPTION_CODE,e.getMessage());
        }
    }

    private ResponseData request()  {
        if(!connSuccess){
            //如果连接失败，直接返回相应提示
            return getResponseData(responseCode,
                    "");
        }
        if (!Utils.isNetworkAvailable()) {
            //无网络连接返回无网络提示
            return getResponseData(NetCodeStatus.NETWORK_EXCEPTION_CODE,
                    "");
        }
        RequestMethod method = mHttpBuilder.getMethod();
        boolean isDoInput = method == RequestMethod.POST;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        String tempLine = null;
        StringBuilder resultBuffer = new StringBuilder();
        try {
            if (isDoInput) {
                mConnection.setDoOutput(true);
            }
            mConnection.setDoInput(true);
            mConnection.setUseCaches(false);
            mConnection.connect();
            if (isDoInput && !Utils.isNullOrEmpty(mHttpBuilder.getBodyString())) {
                outputStream = mConnection.getOutputStream();
                outputStream.write(mHttpBuilder.getBodyString().getBytes(CHARSET));
                outputStream.flush();
            }
            responseCode = mConnection.getResponseCode();
            if (responseCode >= 300) {
                inputStream = mConnection.getErrorStream();
                inputStreamReader = new InputStreamReader(inputStream, CHARSET);
                reader = new BufferedReader(inputStreamReader);
                while ((tempLine = reader.readLine()) != null) {
                    resultBuffer.append(tempLine);
                }
            } else{
                inputStream = mConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream, CHARSET);
                reader = new BufferedReader(inputStreamReader);
                while ((tempLine = reader.readLine()) != null) {
                    resultBuffer.append(tempLine);
                }
            }
        } catch (SocketTimeoutException e){
            //连接超时提示
            responseCode = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
        } catch (IOException e){
            //IO异常提示
            responseCode = NetCodeStatus.FILE_IO_EXCEPTION_CODE;
        }catch (Exception e){
            //其他异常未知错误
            e.printStackTrace();
        }finally {
            close(outputStream, reader, inputStreamReader, inputStream);
        }
        return getResponseData(responseCode, resultBuffer.toString());
    }

    private void close(OutputStream outputStream,
                       BufferedReader reader,
                       InputStreamReader inputStreamReader,
                       InputStream inputStream) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建网络请求返回对象
     *
     * @param code
     * @param message
     * @return
     */
    private ResponseData getResponseData(int code, String message) {
        return new ResponseData(code,message);
    }
}

