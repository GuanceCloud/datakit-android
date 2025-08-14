package com.ft.sdk.sessionreplay.internal.persistence;

import java.io.File;
import java.util.Objects;

public class BatchId {

    private final String id;

    public BatchId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean matchesFile(File file) {
        return extractFileId(file).equals(id);
    }

    public static BatchId fromFile(File file) {
        return new BatchId(extractFileId(file));
    }

    private static String extractFileId(File file) {
        return file.getAbsolutePath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchId batchId = (BatchId) o;
        return id.equals(batchId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
