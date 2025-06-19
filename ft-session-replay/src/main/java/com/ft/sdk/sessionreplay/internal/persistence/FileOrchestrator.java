package com.ft.sdk.sessionreplay.internal.persistence;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface FileOrchestrator {

    /**
     * @param forceNewFile if `true` will force the orchestrator to start a new file.
     *                     By default this flag is `false`.
     * @return a File with enough space to write data, or null if no space is available
     * or the disk can't be written to.
     */
    File getWritableFile(boolean forceNewFile);

    /**
     * @param excludeFiles a set of files to exclude from the readable files
     * @return a File that can be read from, or null is no file is available yet.
     */
    File getReadableFile(Set<File> excludeFiles);

    /**
     * @return a List of all flushable files. A flushable file is any file (readable or writable)
     * which contains valid data and is ready to be uploaded to the events endpoint.
     */
    List<File> getFlushableFiles();

    /**
     * @return a list of files in this orchestrator (both writable and readable)
     */
    List<File> getAllFiles();

    /**
     * @return the root directory of this orchestrator, or null if the root directory is not
     * available (e.g.: because of a SecurityException)
     */
    File getRootDir();

    /**
     * @return the metadata file for a given file, or null if there is no such.
     */
    File getMetadataFile(File file);

    /**
     *
     */
    File getSessionOnError(File file);
}
