package com.ft.sdk.garble.bean;

public class NetworkStateBean {
    private boolean isNetworkAvailable = false;

    private String networkType = "";


    public boolean isNetworkAvailable() {
        return isNetworkAvailable;
    }

    public void setNetworkAvailable(boolean networkAvailable) {
        isNetworkAvailable = networkAvailable;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public void setNetworkNotAvailable() {
        networkType = "";
        isNetworkAvailable = false;
    }
}
