package com.ft.sdk.api;

import android.content.Context;

public interface SdkCore {

    void init(Context context);

    /**
     * Gets the name of the current SDK instance.
     */
    String getName();


    /**
     * Gets the name of the service (given during the SDK initialization, otherwise package name is used).
     */
    String getService();

}
