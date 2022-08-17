package com.ft.plugin.garble;

public class ProductFlavorModel {

    private final String name;
    private Boolean autoUploadMap; //是否自动上 map 文件
    private Boolean autoUploadNativeDebugSymbol;//是否上传 native debug symbol
    private String datakitDCAUrl;
    private String appId;
    private String env;

    public ProductFlavorModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Boolean isAutoUploadMap() {
        return autoUploadMap;
    }

    public void setAutoUploadMap(boolean autoUploadMap) {
        this.autoUploadMap = autoUploadMap;
    }

    public Boolean isAutoUploadNativeDebugSymbol() {
        return autoUploadNativeDebugSymbol;
    }

    public void setAutoUploadNativeDebugSymbol(boolean autoUploadNativeDebugSymbol) {
        this.autoUploadNativeDebugSymbol = autoUploadNativeDebugSymbol;
    }

    public String getDatakitDCAUrl() {
        return datakitDCAUrl;
    }

    public void setDatakitDCAUrl(String datakitDCAUrl) {
        this.datakitDCAUrl = datakitDCAUrl;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public void setFromFTExtension(FTExtension extension) {
        this.env = extension.env;
        this.appId = extension.appId;
        this.autoUploadNativeDebugSymbol = extension.autoUploadNativeDebugSymbol;
        this.autoUploadMap = extension.autoUploadMap;
        this.datakitDCAUrl = extension.datakitDCAUrl;
    }

    public void mergeFTExtension(FTExtension extension) {
        if (this.env == null) {
            this.env = extension.env;
        }
        if (this.appId == null) {
            this.appId = extension.appId;
        }
        if (this.autoUploadMap == null) {
            this.autoUploadMap = extension.autoUploadMap;
        }
        if (this.autoUploadNativeDebugSymbol == null) {
            this.autoUploadNativeDebugSymbol = extension.autoUploadNativeDebugSymbol;
        }
        if (this.datakitDCAUrl == null) {
            this.datakitDCAUrl = extension.datakitDCAUrl;
        }
    }

    @Override
    public String toString() {
        return "ProductFlavorModel{" +
                "name='" + name + '\'' +
                ", autoUploadMap=" + autoUploadMap +
                ", autoUploadNativeDebugSymbol=" + autoUploadNativeDebugSymbol +
                ", datakitDCAUrl='" + datakitDCAUrl + '\'' +
                ", appId='" + appId + '\'' +
                ", env='" + env + '\'' +
                '}';
    }
}