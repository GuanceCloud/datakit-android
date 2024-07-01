package com.ft.plugin.garble;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

/**
 * BY huangDianHua
 * DATE:2019-11-29 14:50
 * Description: gradle plugin extension 配置
 */
public class FTExtension {
    /**
     * 是否显示日志
     */
    public boolean showLog = false;
    /**
     * 是否打开自动track
     */
    public boolean openAutoTrack = true;
    /**
     * 是否自动上 map 文件
     */
    public boolean autoUploadMap = false;
    /**
     * 是否上传 native debug symbol
     */
    public boolean autoUploadNativeDebugSymbol = false;
    /**
     *  datakit DCA 上传地址，一般为 datakitUrl  9531 端口
     */
    public String datakitDCAUrl = "";
    /**
     *
     */
    public String appId = "";
    /**
     *
     */
    public String env = "prod";

    /**
     * asm 版本，默认为 asm9, 支持 asm7 ～ asm9
     */
    public String asmVersion = "asm9";

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
                ", datakitDCAUrl='" + datakitDCAUrl + '\'' +
                ", appId='" + appId + '\'' +
                ", env='" + env + '\'' +
                ", asmVersion='" + asmVersion + '\'' +
                '}';
    }
}
