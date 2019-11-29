package com.ft.plugin.garble;

import com.ft.plugin.garble.asm.RunVariant;

import java.util.ArrayList;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-11-29 14:50
 * Description:
 */
public class FTExtension {
    public RunVariant runVariant = RunVariant.ALWAYS;
    public List<String> whitelist = new ArrayList<>();
    public List<String> blacklist = new ArrayList<>();
    public boolean duplcatedClassSafeMode = false;

    @Override
    public String toString() {
        return "TimingHunterExtension{" +
                "runVariant=" + runVariant +
                ", whitelist=" + whitelist +
                ", blacklist=" + blacklist +
                ", duplcatedClassSafeMode=" + duplcatedClassSafeMode +
                '}';
    }
}
