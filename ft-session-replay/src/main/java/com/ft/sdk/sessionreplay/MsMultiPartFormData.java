package com.ft.sdk.sessionreplay;

import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * 表单提交
 */
public class MsMultiPartFormData {
    public static final String USER_AGENT = "User-Agent";
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private static final String KEY_HEADER_PKG_ID = "X-Pkg-Id";
    private final HttpURLConnection httpConn;
    private final String charset;
    private final OutputStream outputStream;
    private final PrintWriter writer;

    /**
     * multipart/form-data 表单请求，构造h
     *
     * @param requestURL 请求地址
     * @param charset    数据编码
     * @throws java.io.IOException
     */
    public MsMultiPartFormData(String requestURL, String charset, String userAgent, String pkgId)
            throws IOException {
        this.charset = charset;

        // creates a unique boundary based on time stamp
        boundary = "------" + System.currentTimeMillis();

        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        httpConn.setRequestProperty(USER_AGENT, userAgent);
        if (pkgId != null) {
            httpConn.setRequestProperty(KEY_HEADER_PKG_ID, pkgId);
        }
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                true);
    }

    private String getContentType(String fileName) {
        String contentType = URLConnection.guessContentTypeFromName(fileName);
        return contentType == null ? "application/octet-stream" : contentType;
    }

    /**
     * 添加 Field
     *
     * @param name  field 参数名
     * @param value field 参数数值
     */
    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }


    /**
     * 添加表单文件
     *
     * @param fieldName 等同于 <input type="file" name="..." />
     * @throws java.io.IOException
     */
    public void addFilePart(String fieldName, InputStream inputStream, String fileName)
            throws IOException {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                        "Content-Disposition: form-data; name=\"" + fieldName
                                + "\"; filename=\"" + fileName + "\""
                )
                .append(LINE_FEED);
        writer.append("Content-Type: " + getContentType(fileName)).append(LINE_FEED);
//        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * 添加表单头参数
     *
     * @param name  头参数名
     * @param value 头参数值
     */
    public void addHeaderField(String name, String value) {
        writer.append(name + ": " + value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * 返回表单提交 Response
     *
     * @return 成功返回 200，错误返回错误内容
     * @throws java.io.IOException
     */
    public Bundle finish() {
        List<String> response = new ArrayList<String>();

//        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        int code = 0;
        String result = "";
        BufferedReader reader = null;
        try {
            code = httpConn.getResponseCode();
            reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();

        } catch (IOException e) {
            try {
                code = httpConn.getResponseCode();
                reader = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    response.add(line);
                }
                reader.close();
            } catch (IOException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } finally {
            httpConn.disconnect();
        }

        for (String str : response) {
            result += str;
        }

        Bundle returns = new Bundle();
        returns.putInt("code", code);
        returns.putString("response", result);
        return returns;
    }


}