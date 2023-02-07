package com.ft.sdk.garble.bean;

/**
 * create: by huangDianHua
 * time: 2020/6/9 14:16:13
 * description: 日志状态等级
 */
public enum Status {
    /**
     * 提示
     */
    INFO("info"),
    /**
     * 警告
     */
    WARNING("warning"),
    /**
     * 错误
     */
    ERROR("error"),
    /**
     * 严重
     */
    CRITICAL("critical"),
    OK("ok");//恢复
    public String name;

    Status(String name) {
        this.name = name;
    }
}
