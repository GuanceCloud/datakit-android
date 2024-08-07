package com.ft.sdk.sessionreplay.internal.processor;


import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.RumContextProvider;
import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;
import com.ft.sdk.sessionreplay.utils.TimeProvider;

public class RumContextDataHandler {
    private static final String TAG = "RumContextDataHandler";
    private final RumContextProvider rumContextProvider;
    private final TimeProvider timeProvider;
    private final InternalLogger internalLogger;

    public RumContextDataHandler(RumContextProvider rumContextProvider, TimeProvider timeProvider,
                                 InternalLogger internalLogger) {
        this.rumContextProvider = rumContextProvider;
        this.timeProvider = timeProvider;
        this.internalLogger = internalLogger;
    }

    public RecordedQueuedItemContext createRumContextData() {
        // We will make sure we get the timestamp on the UI thread to avoid time skewing
        long timestamp = timeProvider.getDeviceTimestamp();

        // TODO RUM-836 Fetch the RumContext from the core SDKContext when available
        SessionReplayRumContext newRumContext = rumContextProvider.getRumContext();

        if (!newRumContext.isValid()) {
            internalLogger.e(TAG, String.format(INVALID_RUM_CONTEXT_ERROR_MESSAGE_FORMAT, newRumContext.toString()));
            return null;
        }

        return new RecordedQueuedItemContext(timestamp, newRumContext.clone());
    }

    public static final String INVALID_RUM_CONTEXT_ERROR_MESSAGE_FORMAT = "SR RumContextDataHandler: Invalid RUM " +
            "context: [%s] when trying to bundle the RumContextData";
}
