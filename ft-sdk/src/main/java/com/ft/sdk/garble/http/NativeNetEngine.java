package com.ft.sdk.garble.http;

import static com.ft.sdk.garble.http.NetCodeStatus.UNKNOWN_EXCEPTION_CODE;

import android.util.Pair;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
 * Description: Native HttpUrlConnection network request engine
 */
public class NativeNetEngine implements INetEngine {
    public static final String TAG = Constants.LOG_TAG_PREFIX + "NativeNetEngine";
    /**
     * Character encoding, UTF8
     */
    final String CHARSET = "UTF-8";
    /**
     * Parameter wrapper class
     */
    HttpBuilder mHttpBuilder;
    /**
     * Network connection
     */
    HttpURLConnection mConnection;
    /**
     * Connection status (true - success, false - failure)
     */
    boolean connSuccess = false;
    /**
     * Network request response code
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
     * Open connection
     *
     * @return
     */
    private boolean openConnection() {
        try {
            final URL url = new URL(mHttpBuilder.getUrl());
            // Open connection
            mConnection = (HttpURLConnection) url.openConnection();
            if (mConnection == null) {
                // Connection open failure prompt
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
     * Set some common properties
     */
    private void setCommonParams() {
        try {
            // Set connection method
            mConnection.setRequestMethod(mHttpBuilder.getMethod().method);
        } catch (ProtocolException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        // Set connection and read timeout
        mConnection.setConnectTimeout(mHttpBuilder.getHttpConfig().getSendOutTime());
        mConnection.setReadTimeout(mHttpBuilder.getHttpConfig().getReadOutTime());
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
            // If connection fails, return response prompt directly
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
            // Get Content-Type
            String contentType = mHttpBuilder.getHeadParams().get(Constants.SYNC_DATA_CONTENT_TYPE_HEADER);
            // Handle multipart/form-data
            if (isDoInput && "multipart/form-data".equalsIgnoreCase(contentType)) {
                MsMultiPartFormData formData = new MsMultiPartFormData(mConnection);

                // Add form fields
                HashMap<String, String> formFields = mHttpBuilder.getFormParams();
                for (Map.Entry<String, String> field : formFields.entrySet()) {
                    formData.addFormField(field.getKey(), field.getValue());
                }

                // Add file part
                HashMap<String, Pair<String, byte[]>> fileParams = mHttpBuilder.getFileParams();
                for (Map.Entry<String, Pair<String, byte[]>> fileEntry : fileParams.entrySet()) {
                    formData.addFilePart(fileEntry.getKey(), new ByteArrayInputStream(fileEntry.getValue().second)
                            , fileEntry.getValue().first);
                }

                formData.finish();
            } else if (isDoInput && !Utils.isNullOrEmpty(mHttpBuilder.getBodyString())) {
                mConnection.connect();
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
            // Connection timeout prompt
            responseCode = NetCodeStatus.FILE_TIMEOUT_CODE;
            return new FTResponseData(responseCode, e.getLocalizedMessage() + ", network timeout");
        } catch (IOException e) {
            // IO exception prompt
            responseCode = NetCodeStatus.FILE_IO_EXCEPTION_CODE;
            return new FTResponseData(responseCode, e.getLocalizedMessage() + ", check if local network connection is normal");
        } catch (Exception e) {
            // Other unknown errors
            responseCode = NetCodeStatus.UNKNOWN_EXCEPTION_CODE;
            return new FTResponseData(responseCode, e.getLocalizedMessage());

        } finally {
            close(mConnection, outputStream, reader, inputStreamReader, inputStream);
        }
        return new FTResponseData(responseCode, resultBuffer.toString());
    }

    /**
     * Close data connection
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

