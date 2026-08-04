package com.ft.sdk;

import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.bean.ResourceType;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Reports one RUM resource for the duration of an OkHttp WebSocket handshake.
 */
final class FTWebSocketHandshakeResource {
    static final String COLLECTION_LEVEL_HANDSHAKE = "handshake";
    static final String HANDSHAKE_STATE_SUCCESS = "success";
    static final String HANDSHAKE_STATE_REJECTED = "rejected";
    static final String HANDSHAKE_STATE_FAILED = "failed";

    private final String resourceId;
    private final Request request;
    private final long startTimeNano;
    private final AtomicBoolean finished = new AtomicBoolean(false);

    FTWebSocketHandshakeResource(Request request) {
        this.resourceId = Utils.identifyRequest(request);
        this.request = request;
        this.startTimeNano = System.nanoTime();
        FTRUMGlobalManager.get().startResource(resourceId);
    }

    void onOpen(Response response) {
        finish(HANDSHAKE_STATE_SUCCESS, response, null, false);
    }

    void onFailure(Throwable throwable, Response response) {
        String state = getFailureState(response);
        finish(state, response, throwable, HANDSHAKE_STATE_FAILED.equals(state));
    }

    static String getFailureState(Response response) {
        if (response != null && response.code() != 101) {
            return HANDSHAKE_STATE_REJECTED;
        }
        return HANDSHAKE_STATE_FAILED;
    }

    private void finish(String state, Response response, Throwable throwable, boolean forceNetworkError) {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        try {
            long finishTimeNano = System.nanoTime();
            FTRUMGlobalManager.get().stopResource(resourceId);
            FTRUMGlobalManager.get().addResource(resourceId,
                    createResourceParams(state, response, throwable, forceNetworkError),
                    createNetStatus(finishTimeNano));
        } catch (Throwable e) {
            LogUtils.e(FTSdk.TAG, LogUtils.getStackTraceString(e));
        }
    }

    ResourceParams createResourceParams(String state, Response response, Throwable throwable,
                                        boolean forceNetworkError) {
        ResourceParams params = new ResourceParams();
        params.url = request.url().toString();
        params.requestHeader = request.headers().toString();
        params.resourceMethod = request.method();
        params.resourceType = ResourceType.WEBSOCKET.getValue();
        params.resourceWebSocketCollectionLevel = COLLECTION_LEVEL_HANDSHAKE;
        params.resourceWebSocketHandshakeState = state;
        params.enableResourceSize = false;
        params.forceNetworkError = forceNetworkError;

        if (response != null) {
            params.responseHeader = response.headers().toString();
            params.responseConnection = response.header("Connection");
            params.resourceStatus = response.code();
            Protocol protocol = response.protocol();
            params.resourceProtocol = protocol == null ? "" : protocol.toString();
        }
        if (throwable != null) {
            params.requestErrorMsg = throwable.getMessage() == null ? "" : throwable.getMessage();
            params.requestErrorStack = LogUtils.getStackTraceString(throwable);
        }
        return params;
    }

    NetStatusBean createNetStatus(long finishTimeNano) {
        NetStatusBean netStatus = new NetStatusBean();
        netStatus.callStartTime = startTimeNano;
        netStatus.headerStartTime = finishTimeNano;
        netStatus.bodyStartTime = finishTimeNano;
        netStatus.bodyEndTime = finishTimeNano;
        return netStatus;
    }
}
