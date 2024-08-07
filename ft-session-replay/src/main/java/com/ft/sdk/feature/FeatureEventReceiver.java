package com.ft.sdk.feature;

public interface FeatureEventReceiver {

    /**
     * Method invoked when event is received. It will be invoked on the thread which sent event.
     *
     * @param event Incoming event.
     */
    void onReceive(Object event);
}
