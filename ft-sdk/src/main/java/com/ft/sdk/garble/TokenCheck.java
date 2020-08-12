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
    public static final String TAG = "TokenCheck";
    private volatile static TokenCheck tokenCheck;
    private volatile boolean tokenAllowable;
    private volatile boolean isRunning;
    public volatile String message = "";

    private TokenCheck() {
    }

    public synchronized static TokenCheck get() {
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
        if (isRunning) {
            return false;
        }
        isRunning = true;
        String token = FTHttpConfig.get().dataWayToken;
        if (!Utils.isNullOrEmpty(token)) {
            ResponseData result = HttpBuilder.Builder()
                    .setHost(FTHttpConfig.get().serverUrl)
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
                    } else {
                        LogUtils.w(TAG,"Dataflux SDK 未能验证通过您配置的 token");
                        tokenAllowable = false;
                        message = data;
                    }
                } catch (Exception e) {
                    LogUtils.w(TAG,"Dataflux SDK 未能验证通过您配置的 token,message:" + e.getLocalizedMessage());
                    message = e.getLocalizedMessage();
                    tokenAllowable = false;
                }
            } else {
                LogUtils.w(TAG,"Dataflux SDK 未能验证通过您配置的 token");
                tokenAllowable = false;
                message = result.getData();
            }
        } else {
            tokenAllowable = true;
        }
        isRunning = false;
        return tokenAllowable;
    }
}
