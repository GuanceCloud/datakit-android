package com.ft.plugin.garble;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-11-29 14:50
 * Description: gradle plugin extension configuration
 */
public class FTExtension {
    /**
     * Whether to show logs
     */
    public boolean showLog = false;
    /**
     * Whether to enable auto track
     */
    public boolean openAutoTrack = true;
    /**
     * Whether to automatically upload map files
     */
    public boolean autoUploadMap = false;

    /**
     * Whether to upload native debug symbol
     */
    public boolean autoUploadNativeDebugSymbol = false;
    /**
     * datakit upload address, usually datakitUrl port
     */
    public String datakitUrl = "";

    /**
     * dataway token
     */
    public String datawayToken = "";
    /**
     * Application appid
     */
    public String appId = "";
    /**
     * sourcemap upload environment
     */
    public String env = "prod";

    /**
     * asm version, default is asm9, supports asm7 ~ asm9
     */
    public String asmVersion = "asm9";

    /**
     * Ignored package name paths, e.g.: ['com.ft','com/ft'], both are equivalent, the order of effect follows the order in the list
     */
    public List<String> ignorePackages = new ArrayList<>();

    /**
     * Default is [project]/build/intermediates/merged_native_libs
     */
    public String nativeLibPath = "";

    /**
     * Whether to only generate sourcemap
     */
    public boolean generateSourceMapOnly = false;

    /**
     * Release version flavor configuration, {@link ProductFlavorModel}
     */
    private final NamedDomainObjectContainer<ProductFlavorModel> prodFlavor;

    public FTExtension(Project project) {
        prodFlavor = project.container(ProductFlavorModel.class);
    }

    public NamedDomainObjectContainer<ProductFlavorModel> getOther() {
        return prodFlavor;
    }

    public void prodFlavors(Action<NamedDomainObjectContainer<ProductFlavorModel>> action) {
        action.execute(prodFlavor);
    }

    @Override
    public String toString() {
        return "FTExtension{ " +
                "showLog=" + showLog +
                ", openAutoTrack=" + openAutoTrack +
                ", autoUploadMap=" + autoUploadMap +
                ", autoUploadNativeDebugSymbol=" + autoUploadNativeDebugSymbol +
                ", datakitUrl='" + datakitUrl + '\'' +
                ", datawayToken='" + datawayToken + '\'' +
                ", appId='" + appId + '\'' +
                ", env='" + env + '\'' +
                ", asmVersion='" + asmVersion + '\'' +
                ", generateSourceMapOnly='" + generateSourceMapOnly + '\'' +
                '}';
    }
}
