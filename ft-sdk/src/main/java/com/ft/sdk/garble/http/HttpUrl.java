package com.ft.sdk.garble.http;

/**
 * author: huangDianHua
 * time: 2020/9/16 14:16:58
 * description:用于内部地址调用构建
 */
public class HttpUrl {
    private final String host;
    private final String path;
    private final int port;
    private final String holeUrl;

    public HttpUrl(String host, String path, int port, String holeUrl) {
        this.host = host;
        this.path = path;
        this.port = port;
        this.holeUrl = holeUrl;
    }

    /**
     * url host
     * @return
     */
    public String getHost() {
        return host;
    }


    /**
     * url path
     * @return
     */
    public String getPath() {
        return path;
    }


    /**
     * 地址端口
     * @return
     */
    public int getPort() {
        return port;
    }


    public String getHoleUrl() {
        return holeUrl;
    }
}
