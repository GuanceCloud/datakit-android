package com.ft.sdk.garble.http;

/**
 * BY huangDianHua
 * DATE:2019-12-10 15:14
 * Description:
 */
public class ResponseData {
    protected int httpCode;
    protected String data;

    public ResponseData(int httpCode, String data) {
        this.httpCode = httpCode;
        this.data = data;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public String getData() {
        return data;
    }
}
