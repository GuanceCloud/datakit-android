package com.ft.sdk.storage;

public interface DataStoreHandler {

    /**
     * Write data to the datastore.
     * This executes on a worker thread and not on the caller thread.
     *
     * @param <T> datatype of the data to write to the datastore.
     * @param key name of the datastore entry.
     * @param data to write.
     * @param version optional version for the entry.
     * If not specified will give the entry version 0 - even if that would be a downgrade from the previous version.
     * @param callback (optional) to indicate whether the operation succeeded or not.
     * @param serializer to use to serialize the data.
     */
    <T extends Object> void setValue(
            String key,
            T data,
            Integer version,
            DataStoreWriteCallback callback,
            Serializer<T> serializer);

    /**
     * Read data from the datastore.
     * This executes on a worker thread and not on the caller thread.
     *
     * @param <T> datatype of the data to read from the datastore.
     * @param key name of the datastore entry.
     * @param version optional version to use when reading from the datastore.
     * If specified, will only return data if the persistent entry exactly matches this version number.
     * @param callback to return result asynchronously.
     * @param deserializer to use to deserialize the data.
     */
    <T extends Object> void value(
            String key,
            Integer version,
            DataStoreReadCallback<T> callback,
            Deserializer<String, T> deserializer);

    /**
     * Remove an entry from the datastore.
     * This executes on a worker thread and not on the caller thread.
     *
     * @param key name of the datastore entry
     * @param callback (optional) to indicate whether the operation succeeded or not.
     */
    void removeValue(
            String key,
            DataStoreWriteCallback callback);

    /**
     * Removes all saved datastore entries.
     */
    void clearAllData();

    public static final int CURRENT_DATASTORE_VERSION = 0;
}