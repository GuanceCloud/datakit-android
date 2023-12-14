package com.ft.sdk.garble.http;

import android.util.Log;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * BY huangDianHua
 * DATE:2019-12-16 16:15
 * Description:
 */
public class FTResponseData {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTResponseData";
    /**
     * http code
     */
    private int code;
    /**
     * 错误 code， 一部分是 datakit 网络请求返回的错误代码，一部分是 {@link NetCodeStatus} 返回的错误码
     */
    private String errorCode;
    /**
     * 请求错误简要信息
     */
    private String message;

    public FTResponseData(int httpCode, String data) {
        if (httpCode == HttpURLConnection.HTTP_OK) {
            code = httpCode;
        } else {
            try {
                if (data != null) {
                    JSONObject jsonObject = new JSONObject(data);
                    code = jsonObject.optInt("code", httpCode);
                    errorCode = jsonObject.optString("errorCode");
                    message = jsonObject.optString("message");
                }
            } catch (JSONException e) {
                code = NetCodeStatus.NET_STATUS_RESPONSE_NOT_JSON;
                errorCode = NetCodeStatus.NET_STATUS_RESPONSE_NOT_JSON_ERR;
                message = data;
            } catch (Exception e) {
                LogUtils.e(TAG, Log.getStackTraceString(e));
            }

        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
