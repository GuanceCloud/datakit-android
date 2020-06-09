package com.ft.sdk.garble.bean;

/**
 * create: by huangDianHua
 * time: 2020/6/9 14:16:13
 * description:
 */
public enum Status {
    INFO("info"),WARNING("warning"),ERROR("error"),CRITICAL("critical"),OK("ok");
    public String name;
    Status(String name){
        this.name = name;
    }
}
