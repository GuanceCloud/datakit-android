package com.ft.plugin.garble;

import java.util.ArrayList;
import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-11-29 14:50
 * Description:
 */
public class FTExtension {
    public boolean showLog = false;
    public boolean canHookMethod= true;
    public List<String> whitelist = new ArrayList<>();
    public List<String> blacklist = new ArrayList<>();

    @Override
    public String toString() {
        return "FTExtension{" +
                ", whitelist=" + whitelist +
                ", showLog=" + showLog +
                ", canHookMethod=" + canHookMethod +
                ", blacklist=" + blacklist +
                '}';
    }
}
