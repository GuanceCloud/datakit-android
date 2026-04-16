package com.ft.sdk;

import com.ft.sdk.garble.http.EngineFactory;
import com.ft.sdk.garble.http.FTResponseData;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.INetEngine;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * create: by huangDianHua
 * time: 2020/4/21 16:50:18
 * description:
 */
public class NetProxy {
    public final static String TAG = Constants.LOG_TAG_PREFIX + "NetProxy";
    /**
     * Content type
     */
    private static final String CONTENT_TYPE = "text/plain";
    /**
     * Character encoding
     */
    private static final String CHARSET = "UTF-8";
    private final HttpBuilder httpBuilder;
    private final INetEngine engine;

    public NetProxy(HttpBuilder httpBuilder) {
        this.httpBuilder = httpBuilder;
        engine = EngineFactory.createEngine();
        try {
            engine.defaultConfig(httpBuilder);
        } catch (Exception e) {
            LogUtils.e(TAG, e.getLocalizedMessage());
        }
    }

    /**
     * Execute network request
     *
     * @return
     */
    public FTResponseData execute() {
        if (!FTNetworkListener.get().getNetworkStateBean().isNetworkAvailable()) {
            return new FTResponseData(NetCodeStatus.NETWORK_EXCEPTION_CODE, "");
        }
        if (!httpBuilder.getUrl().startsWith("http://") && !httpBuilder.getUrl().startsWith("https://")) {
            //If the request address is empty, prompt an error
            return new FTResponseData(NetCodeStatus.INVALID_PARAMS_EXCEPTION_CODE, "Request address error, check address http(s) scheme");
        }
        setHeadParams();
        if (BuildConfig.DEBUG) {
            LogUtils.d(TAG, "Request curl: " + new CurlBuilder(httpBuilder).toCommand());
        }
        engine.createRequest(httpBuilder);
        return engine.execute();
    }

    /**
     * Convert time format
     */
    private String calculateDate() {
        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(currentTime);
    }

    /**
     * Set request Head parameters
     */
    private void setHeadParams() {
        HashMap<String, String> head = httpBuilder.getHeadParams();
        if (head == null) {
            head = new HashMap<>();
        }
        head.put("Accept-Language", "zh-CN");
        if (!head.containsKey(Constants.SYNC_DATA_USER_AGENT_HEADER)) {
            head.put(Constants.SYNC_DATA_USER_AGENT_HEADER, httpBuilder.getHttpConfig().getUserAgent());
        }
        if (!head.containsKey(Constants.SYNC_DATA_CONTENT_TYPE_HEADER)) {
            head.put(Constants.SYNC_DATA_CONTENT_TYPE_HEADER, CONTENT_TYPE);
        }
        head.put("charset", CHARSET);
        //Add date request header
        head.put("Date", calculateDate());
        httpBuilder.setHeadParams(head);
    }

    static final class CurlBuilder {
        private static final String CONTENT_TYPE = "Content-Type";
        private static final String MULTIPART_FORM_DATA = "multipart/form-data";
        private final HttpBuilder httpBuilder;

        CurlBuilder(HttpBuilder httpBuilder) {
            this.httpBuilder = httpBuilder;
        }

        String toCommand() {
            List<String> parts = new ArrayList<>();
            parts.add("curl");
            parts.add("-X " + httpBuilder.getMethod().name());

            appendHeaders(parts);
            appendBody(parts);
            parts.add(quote(httpBuilder.getUrl()));

            return join(parts);
        }

        private void appendHeaders(List<String> parts) {
            HashMap<String, String> headers = httpBuilder.getHeadParams();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                parts.add("-H " + quote(entry.getKey() + ": " + entry.getValue()));
            }
            if (!headers.containsKey(CONTENT_TYPE) && !isMultipartRequest()) {
                parts.add("-H " + quote(CONTENT_TYPE + ": " + NetProxy.CONTENT_TYPE));
            }
        }

        private void appendBody(List<String> parts) {
            if (httpBuilder.getMethod() != RequestMethod.POST) {
                return;
            }

            if (isMultipartRequest()) {
                appendMultipartBody(parts);
                return;
            }

            String body = httpBuilder.getBodyString();
            if (body != null && !body.isEmpty()) {
                parts.add("--data-raw " + quote(body));
            }
        }

        private void appendMultipartBody(List<String> parts) {
            for (Map.Entry<String, String> entry : httpBuilder.getFormParams().entrySet()) {
                parts.add("-F " + quote(entry.getKey() + "=" + entry.getValue()));
            }

            for (Map.Entry<String, android.util.Pair<String, byte[]>> entry : httpBuilder.getFileParams()) {
                android.util.Pair<String, byte[]> file = entry.getValue();
                String fileName = file != null ? file.first : "binary";
                parts.add("-F " + quote(entry.getKey() + "=@" + fileName));
            }
        }

        private boolean isMultipartRequest() {
            String contentType = httpBuilder.getHeadParams().get(CONTENT_TYPE);
            return contentType != null && contentType.toLowerCase(Locale.US).startsWith(MULTIPART_FORM_DATA);
        }

        private String join(List<String> parts) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < parts.size(); i++) {
                if (i > 0) {
                    builder.append(' ');
                }
                builder.append(parts.get(i));
            }
            return builder.toString();
        }

        private String quote(String value) {
            return "'" + escapeSingleQuotes(value) + "'";
        }

        private String escapeSingleQuotes(String value) {
            if (value == null) {
                return "";
            }
            return value.replace("'", "'\"'\"'");
        }
    }
}
