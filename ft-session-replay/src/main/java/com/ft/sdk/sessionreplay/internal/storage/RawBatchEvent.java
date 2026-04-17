package com.ft.sdk.sessionreplay.internal.storage;

public class RawBatchEvent {

    private final byte[] data;
    private final byte[] metadata;

    public RawBatchEvent(byte[] data, byte[] metadata) {
        this.data = data;
        this.metadata = metadata == null ? EMPTY_BYTE_ARRAY : metadata;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        RawBatchEvent that = (RawBatchEvent) other;

        if (!java.util.Arrays.equals(data, that.data)) return false;
        if (!java.util.Arrays.equals(metadata, that.metadata)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = java.util.Arrays.hashCode(data);
        result = 31 * result + java.util.Arrays.hashCode(metadata);
        return result;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getMetadata() {
        return metadata;
    }

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
}
