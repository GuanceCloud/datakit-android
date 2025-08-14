package com.ft.sdk.storage;

public interface DataStoreWriteCallback {

    /**
     * Triggered on successfully writing data to the datastore.
     */
    public void onSuccess();

    /**
     * Triggered on failing to write data to the datastore.
     */
    public void onFailure();
}