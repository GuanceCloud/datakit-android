package com.ft.plugin.garble;

public class ProductFlavorModel {

    /**
     * favor name
     */
    private final String name;
    /**
     * Whether to automatically upload map file
     */
    private Boolean autoUploadMap;
    /**
     * Whether to upload native debug symbol
     */
    private Boolean autoUploadNativeDebugSymbol;

    /**
     * datakit upload address
     */
    private String datakitUrl;
    /**
     * Workspace dataway token
     */
    private String datawayToken;
    /**
     * User access monitoring, application appid
     */
    private String appId;

    /**
     * zip
     */
    private String zipPath;

    /**
     * Whether to generate only sourcemap
     */
    private Boolean generateSourceMapOnly;

    /**
     * Application development environment
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

    private String nativeLibPath = "";

    public String getNativeLibPath() {
        return nativeLibPath;
    }

    public String getZipPath() {
        return zipPath;
    }

    public void setZipPath(String zipPath) {
        this.zipPath = zipPath;
    }

    public Boolean isGenerateSourceMapOnly() {
        return generateSourceMapOnly;
    }

    public void setFromFTExtension(FTExtension extension) {
        this.env = extension.env;
        this.appId = extension.appId;
        this.autoUploadNativeDebugSymbol = extension.autoUploadNativeDebugSymbol;
        this.autoUploadMap = extension.autoUploadMap;
        this.datakitUrl = extension.datakitUrl;
        this.datawayToken = extension.datawayToken;
        this.nativeLibPath = extension.nativeLibPath;
        this.generateSourceMapOnly = extension.generateSourceMapOnly;
    }

    /**
     * Merge {@link FTExtension} configuration with {@link ProductFlavorModel} parameter configuration, {@link ProductFlavorModel} configuration overrides
     * the configuration in {@link FTExtension}
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

        if (this.nativeLibPath == null) {
            this.nativeLibPath = extension.nativeLibPath;
        }
        if (this.generateSourceMapOnly == null) {
            this.generateSourceMapOnly = extension.generateSourceMapOnly;
        }
    }

    @Override
    public String toString() {
        return "ProductFlavorModel{" +
                "name='" + name + '\'' +
                ", autoUploadMap=" + autoUploadMap +
                ", autoUploadNativeDebugSymbol=" + autoUploadNativeDebugSymbol +
                ", datakitUrl='" + datakitUrl + '\'' +
                ", appId='" + appId + '\'' +
                ", env='" + env + '\'' +
                ", nativeLibPath='" + nativeLibPath + '\'' +
                ", zipPath='" + zipPath + '\'' +
                ", generateSourceZipOnly='" + generateSourceMapOnly + '\'' +
                '}';
    }
}