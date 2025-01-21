package com.ft.plugin.garble;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;

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
     * datakit 上传地址，一般为 datakitUrl  端口
     */
    public String datakitUrl = "";

    /**
     * dataway token
     */
    public String datawayToken = "";
    /**
     * 应用 appid
     */
    public String appId = "";
    /**
     * 上传 sourcemap 上传环境
     */
    public String env = "prod";

    /**
     * asm 版本，默认为 asm9, 支持 asm7 ～ asm9
     */
    public String asmVersion = "asm9";

    /**
     * 忽略的包名路径，例子：['com.ft','com/ft']，两者效果等效，起效顺序按照数据排列顺序
     */
    public List<String> ignorePackages = new ArrayList<>();

    /**
     * 默认认定 [project]/build/intermediates/merged_native_libs
     */
    public String nativeLibPath = "";

    /**
     * 是否只生成 sourcemap
     */
    public boolean generateSourceMapOnly = false;

    /**
     * 发布版本 favor 配置，{@link ProductFlavorModel}
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
