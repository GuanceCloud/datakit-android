package com.ft.sdk.storage;

public interface Deserializer<P, R> {

    /**
     * Deserializes the data from the given payload type into given output type.
     *
     * @return the model represented by the given payload, or null when deserialization
     * is impossible.
     */
    public R deserialize(P model) throws Exception;
}
