package com.ft.sdk.sessionreplay;


public abstract class SystemInfoProxy {

    public abstract boolean isNetworkAvailable();

    public abstract boolean isBatteryHealthToSync();

    public boolean isUploadUrlAvailable() {
        return true;
    }

}
