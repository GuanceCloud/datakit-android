package com.ft.sdk;

/**
 * Supplies an OkHttp {@link okhttp3.EventListener.Factory} for automatic RUM resource tracking.
 * <p>
 * Configure this handler through {@link FTRUMConfig#setOkHttpEventListenerHandler(FTOkHttpEventListenerHandler)}
 * when an application needs to combine SDK timing collection with a custom OkHttp event listener setup.
 */
public interface FTOkHttpEventListenerHandler {

    /**
     * Returns the factory used to create SDK resource event listeners for OkHttp calls.
     */
    FTResourceEventListener.FTFactory getEventListenerFTFactory();

}
