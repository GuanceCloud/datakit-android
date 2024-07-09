package com.ft.sdk.garble.http;

import static com.ft.sdk.garble.http.NetCodeStatus.UNKNOWN_EXCEPTION_CODE;

import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.utils.Constants;
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


/**
 * BY huangDianHua
 * DATE:2019-11-29 18:40
 * Description: 原生 HttpUrlConnection 网络请求引擎
 */
public class NativeNetEngine implements INetEngine {
    public static final String TAG = Constants.LOG_TAG_PREFIX + "NativeNetEngine";
    /**
     * 字符编码，UTF8
     */
    final String CHARSET = "UTF-8";
    /**
     * 参数包装类
     */
    HttpBuilder mHttpBuilder;
    /**
     * 网络连接
     */
    HttpURLConnection mConnection;
    /**
     * 连接状态（true - 成功，false - 失败）
     */
    boolean connSuccess = false;
    /**
     * 网络请求的回复码
     */
    private int responseCode = NetCodeStatus.UNKNOWN_EXCEPTION_CODE;

    /**
     * {@link INetEngine#defaultConfig(HttpBuilder)}
     *
     * @param httpBuilder
     */
    @Override
    public void defaultConfig(HttpBuilder httpBuilder) {
        this.mHttpBuilder = httpBuilder;

    }

    /**
     * {@link INetEngine#createRequest(HttpBuilder)} }
     *
     * @param httpBuilder
     */
    @Override
    public void createRequest(HttpBuilder httpBuilder) {
        openConnection();
        if (connSuccess) {
            setCommonParams();
            setHeadParams();
        }
    }


    /**
     * 打开连接
     *
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
            } else {
                connSuccess = true;
            }
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

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
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        //设置连接和读取超时时间
        mConnection.setConnectTimeout(FTHttpConfigManager.get().getSendOutTime());
        mConnection.setReadTimeout(FTHttpConfigManager.get().getReadOutTime());
    }

    private void setHeadParams() {
        if (mHttpBuilder != null && connSuccess) {
            HashMap head = mHttpBuilder.getHeadParams();
            Set<Map.Entry<String, String>> entries = head.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                mConnection.addRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public FTResponseData execute() {
        try {
            return request();
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
            return new FTResponseData(UNKNOWN_EXCEPTION_CODE, e.getMessage());
        }
    }

    private FTResponseData request() {
        if (!connSuccess) {
            //如果连接失败，直接返回相应提示
            return new FTResponseData(responseCode, "");
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
            } else {
                inputStream = mConnection.getInputStream();
            }
            if (inputStream != null) {
                inputStreamReader = new InputStreamReader(inputStream, CHARSET);
                reader = new BufferedReader(inputStreamReader);
                while ((tempLine = reader.readLine()) != null) {
                    resultBuffer.append(tempLine);
                }
            }
        } catch (SocketTimeoutException e) {
            //连接超时提示
            responseCode = NetCodeStatus.FILE_TIMEOUT_CODE;
            return new FTResponseData(responseCode, e.getLocalizedMessage() + ",网络超时");
        } catch (IOException e) {
            //IO异常提示
            responseCode = NetCodeStatus.FILE_IO_EXCEPTION_CODE;
            return new FTResponseData(responseCode, e.getLocalizedMessage() + ",检查本地网络连接是否正常");
        } catch (Exception e) {
            //其他异常未知错误
            responseCode = NetCodeStatus.UNKNOWN_EXCEPTION_CODE;
            return new FTResponseData(responseCode, e.getLocalizedMessage());

        } finally {
            close(mConnection, outputStream, reader, inputStreamReader, inputStream);
        }
        return new FTResponseData(responseCode, resultBuffer.toString());
    }

    /**
     * 关闭数据连接
     *
     * @param connection
     * @param outputStream
     * @param reader
     * @param inputStreamReader
     * @param inputStream
     */

    private void close(
            HttpURLConnection connection,
            OutputStream outputStream,
            BufferedReader reader,
            InputStreamReader inputStreamReader,
            InputStream inputStream) {
        if (connection != null) {
            connection.disconnect();
        }
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        try {
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
        } catch (IOException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

        }
    }

}

