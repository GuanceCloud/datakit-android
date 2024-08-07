package com.ft.sdk.storage;

public interface Serializer<T> {

    /**
     * Serializes the data into a String.
     * @return the String representing the data or null if any exception occurs
     */
    public String serialize(T model) throws Exception;

    public static final String ERROR_SERIALIZING = "Error serializing %s model";
}