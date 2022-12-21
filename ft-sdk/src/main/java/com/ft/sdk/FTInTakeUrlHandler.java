package com.ft.sdk;

/**
 * 自动埋点功能中，过滤不需要进行采集的地址，一般用于排除非业务相关的一些请求
 *
 * @author Brandon
 */
public interface FTInTakeUrlHandler {
    /**
     * 是否采集这个地址数据
     *
     * @param url url 地址，例子 https://www.guance.com/
     * @return true 采集，false 不采集
     */
    boolean isInTakeUrl(String url);
}
