package com.ft;

import com.ft.sdk.FTMonitor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Handshake;

/**
 * create: by huangDianHua
 * time: 2020/5/18 10:06:37
 * description:
 */
public class OKHttpEventListener extends EventListener {
    @Override
    public void callEnd(@NotNull Call call) {
        super.callEnd(call);
        FTMonitor.get().setResponseEndTime();
    }

    @Override
    public void callFailed(@NotNull Call call, @NotNull IOException ioe) {
        super.callFailed(call, ioe);
        FTMonitor.get().setRequestErrCount();
    }

    @Override
    public void callStart(@NotNull Call call) {
        super.callStart(call);
        FTMonitor.get().setRequestCount();
        FTMonitor.get().setResponseStartTime();
    }

    @Override
    public void dnsEnd(@NotNull Call call, @NotNull String domainName, @NotNull List<InetAddress> inetAddressList) {
        super.dnsEnd(call, domainName, inetAddressList);
        FTMonitor.get().setDnsEndTime();
    }

    @Override
    public void dnsStart(@NotNull Call call, @NotNull String domainName) {
        super.dnsStart(call, domainName);
        FTMonitor.get().setDnsStartTime();
    }

    @Override
    public void secureConnectEnd(@NotNull Call call, @Nullable Handshake handshake) {
        super.secureConnectEnd(call, handshake);
        FTMonitor.get().setTcpEndTime();
    }

    @Override
    public void secureConnectStart(@NotNull Call call) {
        super.secureConnectStart(call);
        FTMonitor.get().setTcpStartTime();
    }
}
