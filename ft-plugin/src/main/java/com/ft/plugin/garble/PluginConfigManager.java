package com.ft.plugin.garble;

import org.objectweb.asm.Opcodes;

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
    }

    /**
     * ASM 版本
     *
     * @return
     */
    public int getASMVersion() {
        String asmVersion = extension.asmVersion;
//        if (asmVersion.equalsIgnoreCase("asm8")) {
//            return Opcodes.ASM8;
//        } else if (asmVersion.equalsIgnoreCase("asm9")) {
//            return Opcodes.ASM9;
//        }
        return Opcodes.ASM7;
    }
}
