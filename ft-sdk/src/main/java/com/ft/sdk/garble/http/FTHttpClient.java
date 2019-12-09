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
        if (connSuccess) {
            calcuteDate();
            mConnection.addRequestProperty("Date", gmtString);
            if (ftHttpConfig.enableRequestSigning) {
                addAuthorizationHead();
            }
        }
    }

    @Override
    protected String getBodyContent() {
        StringBuffer sb = new StringBuffer();
        if (mHttpBuilder.getMethod() == RequestMethod.POST) {
            HashMap<String, Object> param = mHttpBuilder.getParams();
            if (param != null) {
                Iterator<String> keys = param.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (keys.hasNext()) {
                        if(param.get(key) != null) {
                            sb.append(key + "=" +param.get(key) +",");
                        }else{
                            sb.append(key +",");
                        }
                    } else {
                        if(param.get(key) != null) {
                            sb.append(key + "=" +param.get(key));
                        }else{
                            sb.append(key);
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    private void addAuthorizationHead() {
        String akId = ftHttpConfig.akId;
        mConnection.addRequestProperty("Authorization", "DWAY " + akId + ":" + getSignature());
    }

    private String getSignature() {
        String aks = ftHttpConfig.akSecret;
        String method = mHttpBuilder.getMethod().method;
        String contentMD5 = getContentMD5();
        return Utils.getHnacSha1(aks, method + "\n" + contentMD5 + "\n" + CONTENT_TYPE + "\n" + gmtString);
    }

    private String getContentMD5() {
        return Utils.contentMD5Encode(getBodyContent());
    }

    private void calcuteDate() {
        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        gmtString = sdf.format(currentTime);
    }
}
