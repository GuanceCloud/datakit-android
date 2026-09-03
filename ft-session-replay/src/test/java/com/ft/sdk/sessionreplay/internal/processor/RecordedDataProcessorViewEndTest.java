package com.ft.sdk.sessionreplay.internal.processor;

import com.ft.sdk.sessionreplay.internal.async.ViewEndRecordedDataQueueItem;
import com.ft.sdk.sessionreplay.internal.storage.RecordWriter;
import com.ft.sdk.sessionreplay.model.ViewEndRecord;
import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RecordedDataProcessorViewEndTest {

    @Test
    public void processViewEnd_withoutPreviousContextDoesNotWriteARecord() {
        CapturingRecordWriter writer = new CapturingRecordWriter();
        RecordedDataProcessor processor = new RecordedDataProcessor(
                null,
                null,
                null,
                writer,
                null,
                false,
                null
        );

        processor.processViewEnd(new ViewEndRecordedDataQueueItem(1234L));

        assertTrue(writer.records.isEmpty());
    }

    @Test
    public void processViewEnd_closesAndClearsTheNativeReplayContext() throws Exception {
        CapturingRecordWriter writer = new CapturingRecordWriter();
        RecordedDataProcessor processor = new RecordedDataProcessor(
                null,
                null,
                null,
                writer,
                null,
                false,
                null
        );
        Field previousContextField = RecordedDataProcessor.class.getDeclaredField("prevRumContext");
        previousContextField.setAccessible(true);
        previousContextField.set(
                processor,
                new SessionReplayRumContext("application-id", "session-id", "native-view-id")
        );

        processor.processViewEnd(new ViewEndRecordedDataQueueItem(1234L));
        processor.processViewEnd(new ViewEndRecordedDataQueueItem(2345L));

        assertEquals(1, writer.records.size());
        EnrichedRecord enrichedRecord = writer.records.get(0);
        assertEquals("native-view-id", enrichedRecord.getViewId());
        assertEquals(1, enrichedRecord.getRecords().size());
        assertTrue(enrichedRecord.getRecords().get(0) instanceof ViewEndRecord);
        assertEquals(1234L, ((ViewEndRecord) enrichedRecord.getRecords().get(0)).timestamp);
    }

    private static class CapturingRecordWriter implements RecordWriter {
        private final List<EnrichedRecord> records = new ArrayList<>();

        @Override
        public void write(EnrichedRecord record) {
            records.add(record);
        }
    }
}
