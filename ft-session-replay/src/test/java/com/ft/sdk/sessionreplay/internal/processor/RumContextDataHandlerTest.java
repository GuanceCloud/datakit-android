package com.ft.sdk.sessionreplay.internal.processor;

import com.ft.sdk.sessionreplay.utils.RumContextProvider;
import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;
import com.ft.sdk.sessionreplay.utils.TimeProvider;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RumContextDataHandlerTest {

    @Test
    public void capturedContextShouldNotChangeWhenProviderContextChanges() {
        Map<String, Object> globalContext = new HashMap<>();
        globalContext.put("wgt_id", "widget-1");
        SessionReplayRumContext source = new SessionReplayRumContext(
                "app-id",
                "session-id",
                "view-id",
                globalContext
        );
        RumContextDataHandler handler = new RumContextDataHandler(
                new RumContextProvider() {
                    @Override
                    public SessionReplayRumContext getRumContext() {
                        return source;
                    }
                },
                new TimeProvider() {
                    @Override
                    public long getDeviceTimestamp() {
                        return 42L;
                    }
                },
                null
        );

        RecordedQueuedItemContext captured = handler.createRumContextData();
        globalContext.put("wgt_id", "widget-2");

        assertEquals(42L, captured.getTimestamp());
        assertEquals(
                "widget-1",
                captured.getNewRumContext().getGlobalContext().get("wgt_id")
        );
    }
}
