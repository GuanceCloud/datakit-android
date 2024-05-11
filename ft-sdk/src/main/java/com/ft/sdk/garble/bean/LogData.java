package com.ft.sdk.garble.bean;

/**
 * author: huangDianHua
 * time: 2020/8/7 17:09:39
 * description:日志对象(外部使用)
 */
public class LogData {
    /**
     * 日志内容，纯文本或 JSONString 都可以
     */
    String content;
    /**
     * 日志等级
     */
    String status;

    public LogData(String content, Status status) {
        this.content = content;
        this.status = status.name;
    }

    public LogData(String content, String status) {
        this.content = content;
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
