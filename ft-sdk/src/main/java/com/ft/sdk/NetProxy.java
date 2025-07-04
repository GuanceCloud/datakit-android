package com.ft.sdk;

import com.ft.sdk.garble.http.EngineFactory;
import com.ft.sdk.garble.http.FTResponseData;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.INetEngine;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
}
