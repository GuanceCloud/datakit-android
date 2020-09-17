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

    public HttpUrl(String host, String path, int port) {
        this.host = host;
        this.path = path;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
