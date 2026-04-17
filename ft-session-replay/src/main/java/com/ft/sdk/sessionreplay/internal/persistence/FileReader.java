package com.ft.sdk.sessionreplay.internal.persistence;

import androidx.annotation.WorkerThread;
import java.io.File;

/**
 * Interface for reading data from a file.
 * 
 * @param <T> the type of data to be read from the file.
 */
public interface FileReader<T> {

    /**
     * Reads data from the given file.
     * 
     * @param file the file to read from.
     * @return the data read from the file.
     */
    @WorkerThread
    T readData(File file);
}
