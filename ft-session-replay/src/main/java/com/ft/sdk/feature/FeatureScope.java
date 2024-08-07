package com.ft.sdk.feature;

import com.ft.sdk.storage.DataStoreHandler;


public interface FeatureScope {

    /**
     * Property to enable interaction with the data store.
     */
    DataStoreHandler getDataStore();

    /**
     * Utility to write an event, asynchronously.
     *
     * @param forceNewBatch if `true` forces the [EventBatchWriter] to write in a new file and
     *                      not reuse the already existing pending data persistence file. By default it is `false`.
     * @param callback      an operation called with an up-to-date [DatadogContext]
     *                      and an [EventBatchWriter]. Callback will be executed on a worker thread from I/O pool.
     *                      [DatadogContext] will have a state created at the moment this method is called, before the
     *                      thread switch for the callback invocation.
     */
    void withWriteContext(boolean forceNewBatch, DataConsumerCallback callback);

    /**
     * Send event to a given feature. It will be sent in a synchronous way.
     *
     * @param event Event to send.
     */
    void sendEvent(Object event);

    /**
     * Returns the original feature.
     */
    <T extends Feature> T unwrap();
}
