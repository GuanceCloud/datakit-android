package com.ft.plugin.garble;

public class ProductFlavorModel {

    private final String name;
    /**
     * 是否自动上 map 文件
     */
    private Boolean autoUploadMap;
    /**
     * 是否上传 native debug symbol
     */
    private Boolean autoUploadNativeDebugSymbol;

    /**
     *  datakit 上传地址
     */
    private String datakitUrl;
    /**
     * 工作空间 dataway token
     */
    private String datawayToken;
    /**
     * 用户访问监测，应用 appid
     */
    private String appId;

    /**
     * 应用开发环境
     */
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

    public Boolean isAutoUploadNativeDebugSymbol() {
        return autoUploadNativeDebugSymbol;
    }

    public String getDatakitUrl() {
        return datakitUrl;
    }


    public String getDatawayToken() {
        return datawayToken;
    }

    public String getAppId() {
        return appId;
    }

    public String getEnv() {
        return env;
    }

    public void setFromFTExtension(FTExtension extension) {
        this.env = extension.env;
        this.appId = extension.appId;
        this.autoUploadNativeDebugSymbol = extension.autoUploadNativeDebugSymbol;
        this.autoUploadMap = extension.autoUploadMap;
        this.datakitUrl = extension.datakitUrl;
        this.datawayToken = extension.datawayToken;
    }

    /**
     * {@link FTExtension} 配置与 {@link ProductFlavorModel} 参数配置进行合并，{@link ProductFlavorModel}配置覆盖
     * {@link FTExtension} 中的配置
     *
     * @param extension
     */

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
        if (this.datakitUrl == null) {
            this.datakitUrl = extension.datakitUrl;
        }

        if (this.datawayToken == null) {
            this.datawayToken = extension.datawayToken;
        }
    }

    @Override
    public String toString() {
        return "ProductFlavorModel{" +
                "name='" + name + '\'' +
                ", autoUploadMap=" + autoUploadMap +
                ", autoUploadNativeDebugSymbol=" + autoUploadNativeDebugSymbol +
                ", datakitDCAUrl='" + datakitUrl + '\'' +
                ", appId='" + appId + '\'' +
                ", env='" + env + '\'' +
                '}';
    }
}