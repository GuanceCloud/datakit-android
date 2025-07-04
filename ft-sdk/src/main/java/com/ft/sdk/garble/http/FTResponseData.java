package com.ft.sdk.garble.http;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

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
     * Error code, part of the datakit network request error code, part of {@link NetCodeStatus} error code
     */
    private String errorCode;
    /**
     * Request error brief information
     */
    private String message;

    public FTResponseData(int code, String data) {
        if (code == HttpURLConnection.HTTP_OK) {
            this.code = code;
            this.message = data;
        } else {
            if (code < NetCodeStatus.NETWORK_EXCEPTION_CODE) {
                try {
                    if (data != null) {
                        JSONObject jsonObject = new JSONObject(data);
                        this.code = jsonObject.optInt("code", code);
                        this.errorCode = jsonObject.optString("errorCode");
                        if (Utils.isNullOrEmpty(errorCode)) {
                            //dataway error code compatible
                            this.errorCode = jsonObject.optString("error_code");
                        }
                        this.message = jsonObject.optString("message");
                    }
                } catch (JSONException e) {
                    this.code = code;
                    this.message = data;
                } catch (Exception e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                }
            } else {
                this.code = code;
                this.message = data;
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
