package com.ft.sdk.garble.bean;

/**
 * description: Log object (for external use)
 */
public class LogData {
    /**
     * Log content, can be plain text or JSONString
     */
    String content;
    /**
     * Log level
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
