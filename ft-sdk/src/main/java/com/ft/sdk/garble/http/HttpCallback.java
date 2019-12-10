package com.ft.sdk.garble.http;

/**
 * BY huangDianHua
 * DATE:2019-12-09 16:43
 * Description:
 */
public interface HttpCallback<T extends ResponseData> {
    public void onComplete(T result);
}
