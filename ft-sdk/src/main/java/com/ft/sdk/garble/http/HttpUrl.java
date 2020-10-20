package com.ft.sdk.garble.http;

/**
 * author: huangDianHua
 * time: 2020/9/16 14:16:58
 * description:
 */
public class HttpUrl {
    private String host;
    private String path;
    private int port;
    private String holeUrl;

    public HttpUrl(String host, String path, int port, String holeUrl) {
        this.host = host;
        this.path = path;
        this.port = port;
        this.holeUrl = holeUrl;
    }

    public String getHost() {
        return host;
    }


    public String getPath() {
        return path;
    }


    public int getPort() {
        return port;
    }


    public String getHoleUrl() {
        return holeUrl;
    }
}
