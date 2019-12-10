package com.ft.sdk.garble.http;

import org.json.JSONObject;

/**
 * BY huangDianHua
 * DATE:2019-12-10 15:14
 * Description:
 */
public class ResponseData {
    private int code;
    private String errorCode;
    private String message;
    public ResponseData(String data){
        try {
            if (data != null) {
                JSONObject jsonObject = new JSONObject(data);
                code = jsonObject.optInt("code");
                errorCode = jsonObject.optString("errorCode");
                message = jsonObject.optString("message");
            }
        }catch (Exception e){
            e.printStackTrace();
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
