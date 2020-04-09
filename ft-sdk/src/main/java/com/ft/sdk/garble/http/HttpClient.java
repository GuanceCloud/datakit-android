package com.ft.sdk.garble.http;

import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import static com.ft.sdk.garble.http.NetCodeStatus.UNKNOWN_EXCEPTION_CODE;

/**
 * BY huangDianHua
 * DATE:2019-11-29 18:40
 * Description:
 */
public abstract class HttpClient {
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
    //SDK 中网络的配置
    FTHttpConfig ftHttpConfig = FTHttpConfig.get();
    //网络请求的回复码
    private int responseCode = NetCodeStatus.UNKNOWN_EXCEPTION_CODE;

    /**
     * 请求参数中的 body 部分
     * @return
     */
    protected abstract String getBodyContent();

    public HttpClient(HttpBuilder httpBuilder) {
        this.mHttpBuilder = httpBuilder;
        //打开连接
        if (openConnection()) {
            //连接打开后设置公共参数
            setCommonParams();
        }
    }

    /**
     * 打开连接
     * @return
     */
    private boolean openConnection() {
        try {
            //默认使用 SDK 中配置的全局 URL
            String urlStr = ftHttpConfig.metricsUrl;
            if (mHttpBuilder.getUrl() != null) {
                //用当前请求的 URL
                urlStr = mHttpBuilder.getUrl();
            }
            String model = mHttpBuilder.getModel();
            if (model != null && !model.isEmpty()) {
                //在 url 后添加请求的某个模块
                urlStr = urlStr + "/" + model;
            }
            if(Utils.isNullOrEmpty(urlStr)){
                //请求地址为空是提示错误
                connSuccess = false;
                responseCode = HttpURLConnection.HTTP_NOT_FOUND;
                LogUtils.e("请求地址不能为空");
                return false;
            }
            final URL url = new URL(urlStr +"?"+ getQueryString());
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
     * 封装 get 请求参数
     * @return
     */
    private String getQueryString() {
        StringBuffer sb = new StringBuffer();
        if (mHttpBuilder.getMethod() == RequestMethod.GET) {
            HashMap<String, Object> param = mHttpBuilder.getParams();
            if (param != null) {
                Iterator<String> keys = param.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    sb.append("&" + key + "=" + param.get(key));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 设置一些公共属性
     */
    private void setCommonParams() {
        //如果没有设置超时时间就给个10秒默认值
        if (mHttpBuilder.getSendOutTime() == 0) {
            mHttpBuilder.setSendOutTime(10 * 1000);
        }
        if (mHttpBuilder.getReadOutTime() == 0) {
            mHttpBuilder.setReadOutTime(10 * 1000);
        }

        try {
            //设置连接方式
            mConnection.setRequestMethod(mHttpBuilder.getMethod().method);
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        //设置连接和读取超时时间
        mConnection.setConnectTimeout(mHttpBuilder.getSendOutTime());
        mConnection.setReadTimeout(mHttpBuilder.getReadOutTime());
    }

    public <T extends ResponseData> T execute(Class<T> tClass) {
        try {
            return request(tClass);
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
            return getResponseData(tClass,UNKNOWN_EXCEPTION_CODE,e.getMessage());
        }
    }

    private <T extends ResponseData> T request(Class<T> tClass)  {
        if(!connSuccess){
            //如果连接失败，直接返回相应提示
            return getResponseData(tClass,responseCode,
                    "");
        }
        if (!Utils.isNetworkAvailable()) {
            //无网络连接返回无网络提示
            return getResponseData(tClass,NetCodeStatus.NETWORK_EXCEPTION_CODE,
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
            if (isDoInput) {
                outputStream = mConnection.getOutputStream();
                outputStream.write(getBodyContent().getBytes(CHARSET));
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
        if(mHttpBuilder.isShowLog()) {
            LogUtils.d("HTTP-response:[code:" + responseCode + ",response:" + resultBuffer.toString() + "]");
        }
        return getResponseData(tClass,responseCode, resultBuffer.toString());
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
     * @param tClass
     * @param code
     * @param message
     * @param <T>
     * @return
     */
    private <T extends ResponseData> T getResponseData(Class<T> tClass, int code, String message) {
        Constructor[] constructor = tClass.getConstructors();
        for (Constructor<T> con : constructor) {
            Class[] classes = con.getParameterTypes();
            if (classes.length == 2) {
                if (classes[0].getName().equals(int.class.getName()) &&
                        classes[1].getName().equals(String.class.getName())) {
                    try {
                        return con.newInstance(code, message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
