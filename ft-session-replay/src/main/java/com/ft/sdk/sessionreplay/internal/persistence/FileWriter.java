package com.ft.sdk.sessionreplay.internal.persistence;

import java.io.File;

public interface FileWriter<T> {

    /**
     * Writes data as a [T] into a file.
     * 
     * @param file   the file to write to
     * @param data   the data to write
     * @param append whether to append data at the end of the file or overwrite
     * @return whether the write operation was successful
     */
    boolean writeData(File file, T data, boolean append);
}
