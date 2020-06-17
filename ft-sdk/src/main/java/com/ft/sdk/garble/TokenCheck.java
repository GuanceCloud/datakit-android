package com.ft.sdk.garble;

import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

/**
 * create: by huangDianHua
 * time: 2020/6/17 13:48:59
 * description:验证 token 是否合法
 */
public class TokenCheck {
    private static TokenCheck tokenCheck;
    private volatile boolean tokenAllowable;

    private TokenCheck() {
    }

    public static TokenCheck get() {
        if (tokenCheck == null) {
            tokenCheck = new TokenCheck();
        }
        return tokenCheck;
    }

    /**
     * 检测 token 是否合法
     */
    public synchronized boolean checkToken() {
        if (tokenAllowable) {
            return true;
        }
        String token = FTHttpConfig.get().dataWayToken;
        if (!Utils.isNullOrEmpty(token)) {
            ResponseData result = HttpBuilder.Builder()
                    .setHost(FTHttpConfig.get().metricsUrl)
                    .setModel(Constants.URL_MODEL_TOKEN_CHECK + "/" + token)
                    .setMethod(RequestMethod.GET)
                    .enableToken(false)
                    .executeSync(ResponseData.class);
            if (result != null && result.getHttpCode() == 200) {
                String data = result.getData();
                try {
                    JSONObject js = new JSONObject(data);
                    int code = js.optInt("code");
                    if (code == 200) {
                        tokenAllowable = true;
                        return true;
                    } else {
                        LogUtils.w("Dataflux SDK 未能验证通过您配置的 token");
                        tokenAllowable = false;
                        return false;
                    }
                } catch (Exception e) {
                    LogUtils.w("Dataflux SDK 未能验证通过您配置的 token,message:" + e.getLocalizedMessage());
                    tokenAllowable = false;
                    return false;
                }
            } else {
                LogUtils.w("Dataflux SDK 未能验证通过您配置的 token");
                tokenAllowable = false;
                return false;
            }
        } else {
            tokenAllowable = true;
            return true;
        }
    }
}
