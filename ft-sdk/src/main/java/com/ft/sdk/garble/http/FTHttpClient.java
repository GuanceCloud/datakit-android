package com.ft.sdk.garble.http;

import com.ft.sdk.garble.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

/**
 * BY huangDianHua
 * DATE:2019-12-09 16:58
 * Description:
 */
public class FTHttpClient extends HttpClient {
    private String gmtString;

    public FTHttpClient(HttpBuilder httpBuilder) {
        super(httpBuilder);
        if (connSuccess && httpBuilder.isUseDefaultHead()) {
            //设置 DataFlux 请求特有的请求头
            setHeadParams();
            //计算日期
            calculateDate();
            //添加日期请求头
            mConnection.addRequestProperty("Date", gmtString);
            //如果开启了签名，添加签名信息
            if (ftHttpConfig.enableRequestSigning) {
                addAuthorizationHead();
            }
        }
    }

    @Override
    protected String getBodyContent() {
        return mHttpBuilder.getBodyString() == null ? "" : mHttpBuilder.getBodyString();
    }

    private void addAuthorizationHead() {
        String akId = ftHttpConfig.akId;
        mConnection.addRequestProperty("Authorization", "DWAY " + akId + ":" + getSignature());
    }

    /**
     * 获取签名
     * @return
     */
    private String getSignature() {
        String aks = ftHttpConfig.akSecret;
        String method = mHttpBuilder.getMethod().method;
        String contentMD5 = getContentMD5();
        return Utils.getHMacSha1(aks, method + "\n" + contentMD5 + "\n" + CONTENT_TYPE + "\n" + gmtString);
    }

    /**
     * MD5 加密 请求内容
     * @return
     */
    private String getContentMD5() {
        return Utils.contentMD5Encode(getBodyContent());
    }

    /**
     * 转换时间格式
     */
    private void calculateDate() {
        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        gmtString = sdf.format(currentTime);
    }

    /**
     *  设置请求的 Head 参数
     */
    private void setHeadParams() {
        mConnection.addRequestProperty("X-Datakit-UUID", ftHttpConfig.uuid);
        mConnection.addRequestProperty("User-Agent", ftHttpConfig.userAgent);
        mConnection.addRequestProperty("Accept-Language", "zh-CN");
        mConnection.addRequestProperty("Content-Type", CONTENT_TYPE);
        mConnection.addRequestProperty("charset", CHARSET);
        HashMap<String, String> headMap = mHttpBuilder.getHeadParams();
        Iterator<String> keys = headMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            mConnection.addRequestProperty(key, headMap.get(key));
        }
    }
}
