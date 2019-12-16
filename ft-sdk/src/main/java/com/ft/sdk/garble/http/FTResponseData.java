package com.ft.sdk.garble.http;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * BY huangDianHua
 * DATE:2019-12-16 16:15
 * Description:
 */
public class FTResponseData extends ResponseData {
    private int code;
    private String errorCode;
    private String message;

    public FTResponseData(int httpCode, String data) {
        super(httpCode, data);
        if(httpCode == HttpURLConnection.HTTP_OK) {
            try {
                if (data != null) {
                    JSONObject jsonObject = new JSONObject(data);
                    code = jsonObject.optInt("code");
                    errorCode = jsonObject.optString("errorCode");
                    message = jsonObject.optString("message");
                }
            } catch (JSONException e) {
                code = NetCodeStatus.NET_STATUS_RESPONSE_NOT_JSON;
                errorCode = NetCodeStatus.NET_STATUS_RESPONSE_NOT_JSON_ERR;
                message = data;
            } catch (Exception e) {
                e.printStackTrace();
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
