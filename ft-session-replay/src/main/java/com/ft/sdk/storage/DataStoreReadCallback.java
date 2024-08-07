package com.ft.sdk.storage;

public interface DataStoreReadCallback<T> {

    /**
     * Triggered on successfully reading data from the datastore.
     *
     * @param dataStoreContent (nullable) contains the datastore content if there was data to fetch, else null.
     */
    void onSuccess(DataStoreContent<T> dataStoreContent);

    /**
     * Triggered on failing to read data from the datastore.
     */
    void onFailure();
}


