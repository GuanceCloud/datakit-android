package com.ft.plugin.garble;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

/**
 * BY huangDianHua
 * DATE:2019-11-29 14:50
 * Description:
 */
public class FTExtension {
    public boolean showLog = false;//是否显示日志
    public boolean openAutoTrack = true;//是否打开自动track
    public boolean autoUploadMap = false; //是否自动上 map 文件
    public boolean autoUploadNativeDebugSymbol = false;//是否上传 native debug symbol
    public String datakitDCAUrl = "";
    public String appId = "";
    public String env = "prod";

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
                '}';
    }
}
