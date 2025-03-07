package com.ft.sdk;

public class InnerConfigSet {
    public static void enableNetwork() {
        FTNetworkListener.get().getNetworkStateBean().setNetworkAvailable(true);
    }
}
