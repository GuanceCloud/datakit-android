package com.ft.sdk.sessionreplay.internal.persistence;

import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;

import java.io.File;
import java.util.List;

public interface BatchFileReader {

    /**
     * Reads data from the given file.
     * 
     * @param file the file to read from
     * @return the list of events as [RawBatchEvent] data stored in a file.
     */
    List<RawBatchEvent> readData(File file);
}