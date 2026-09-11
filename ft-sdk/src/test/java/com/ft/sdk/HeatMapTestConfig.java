package com.ft.sdk;

import java.lang.reflect.Field;

/** Installs the RUM state without starting unrelated monitors and storage workers. */
final class HeatMapTestConfig {
    static void set(FTRUMConfig config) throws Exception {
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        manager.release();
        Field configField = FTRUMConfigManager.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(manager, config);
        Field enabledField = FTRUMConfigManager.class.getDeclaredField("heatMapEnabled");
        enabledField.setAccessible(true);
        enabledField.setBoolean(manager, config.isRumEnable() && config.isEnableHeatMap());
    }
}
