package com.ft.plugin.garble;

import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局配置管理
 */
public class PluginConfigManager {

    private static class SingletonHolder {
        private static final PluginConfigManager INSTANCE = new PluginConfigManager();
    }

    public static PluginConfigManager get() {
        return PluginConfigManager.SingletonHolder.INSTANCE;
    }

    private FTExtension extension;


    public void setExtension(FTExtension extension) {
        this.extension = extension;
        //默认添加听云
        this.extension.ignorePackages.add(Constants.CLASS_NAME_TING_YUN_PACKAGE);
    }

    /**
     * ASM 版本
     *
     * @return
     */
    public int getASMVersion() {
        try {
            String asmVersion = extension.asmVersion;
            if (asmVersion.equalsIgnoreCase("asm8")) {
                return Opcodes.ASM8;
            } else if (asmVersion.equalsIgnoreCase("asm7")) {
                return Opcodes.ASM7;
            }
        } catch (NullPointerException e) {
            Logger.error("Get FTExtension error: using asm9 default.");
        }

        return Opcodes.ASM9;
    }

    public List<String> getIgnorePackages() {
        if (extension == null) {
            Logger.error("Get FTExtension error: ignore ignorePackages");
            return new ArrayList<>();
        }
        return extension.ignorePackages;
    }
}
