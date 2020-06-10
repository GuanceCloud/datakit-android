package com.ft.sdk.garble.bean;

/**
 * create: by huangDianHua
 * time: 2020/6/9 14:16:13
 * description:
 */
public enum Status {
    INFO("info"),//提示
    WARNING("warning"),//警告
    ERROR("error"),//错误
    CRITICAL("critical"),//严重
    OK("ok");//恢复
    public String name;
    Status(String name){
        this.name = name;
    }
}
