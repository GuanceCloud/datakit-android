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
    Status status = Status.INFO;

    public LogData(String content, Status status) {
        this.content = content;
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
