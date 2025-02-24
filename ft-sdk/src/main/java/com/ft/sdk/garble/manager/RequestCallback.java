package com.ft.sdk.garble.manager;

/**
 * 内部网络请求请求回调 {@link com.ft.sdk.FTTrackInner}，{@link com.ft.sdk.SyncTaskManager}
 */
public interface RequestCallback {
    /**
     * @param code      {@link java.net.HttpURLConnection#HTTP_OK}等数值，当 http 请求发出前发生错误，
     *                  则返回
     *                  {@link com.ft.sdk.garble.http.NetCodeStatus#UNKNOWN_EXCEPTION_CODE },
     *                  如果发生数据内容解析逻辑错误，则返回
     *                  {@link com.ft.sdk.garble.http.NetCodeStatus#INVALID_PARAMS_EXCEPTION_CODE }
     * @param response  当发生错误，返回错误内容，反之返回 http body 内容
     * @param errorCode 服务端返回，json errorCode 数据
     */
    void onResponse(int code, String response, String errorCode);
}
