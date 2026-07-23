package com.ft.sdk.garble.bean;

public class NetworkStateBean {
    private volatile boolean isNetworkAvailable = false;

    private volatile String networkType = "";

    private volatile Boolean networkValidated = null;

    private volatile Integer networkDownlinkKbps = null;

    private volatile Integer networkUplinkKbps = null;

    private volatile Integer networkSignalStrength = null;

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

    public Boolean getNetworkValidated() {
        return networkValidated;
    }

    public void setNetworkValidated(Boolean networkValidated) {
        this.networkValidated = networkValidated;
    }

    public Integer getNetworkDownlinkKbps() {
        return networkDownlinkKbps;
    }

    public void setNetworkDownlinkKbps(Integer networkDownlinkKbps) {
        this.networkDownlinkKbps = networkDownlinkKbps;
    }

    public Integer getNetworkUplinkKbps() {
        return networkUplinkKbps;
    }

    public void setNetworkUplinkKbps(Integer networkUplinkKbps) {
        this.networkUplinkKbps = networkUplinkKbps;
    }

    public Integer getNetworkSignalStrength() {
        return networkSignalStrength;
    }

    public void setNetworkSignalStrength(Integer networkSignalStrength) {
        this.networkSignalStrength = networkSignalStrength;
    }

    public void setNetworkNotAvailable() {
        networkType = "";
        networkValidated = null;
        networkDownlinkKbps = null;
        networkUplinkKbps = null;
        networkSignalStrength = null;
        isNetworkAvailable = false;
    }
}
