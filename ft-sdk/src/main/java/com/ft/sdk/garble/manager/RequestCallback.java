package com.ft.sdk.garble.manager;

/**
 * Internal network request callback {@link com.ft.sdk.FTTrackInner}, {@link com.ft.sdk.SyncTaskManager}
 */
public interface RequestCallback {
    /**
     * @param code      {@link java.net.HttpURLConnection#HTTP_OK} and other values. If an error occurs before the http request is sent,
     *                  returns
     *                  {@link com.ft.sdk.garble.http.NetCodeStatus#UNKNOWN_EXCEPTION_CODE },
     *                  If a data content parsing logic error occurs, returns
     *                  {@link com.ft.sdk.garble.http.NetCodeStatus#INVALID_PARAMS_EXCEPTION_CODE }
     * @param response  When an error occurs, returns the error content, otherwise returns the http body content
     * @param errorCode Returned by the server, json errorCode data
     */
    void onResponse(int code, String response, String errorCode);
}
