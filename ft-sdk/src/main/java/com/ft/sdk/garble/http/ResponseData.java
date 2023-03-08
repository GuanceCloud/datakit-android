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

    /**
     * http code
     * @return
     */
    public int getHttpCode() {
        return httpCode;
    }

    /**
     * http 返回数据内容，字符形式返回
     * @return
     */
    public String getData() {
        return data;
    }
}
