package com.ft.sdk;

/**
 * Action tracking handler
 * 
 */
public interface FTActionTrackingHandler {
    HandlerAction isInTake(ActionEventWrapper actionEventWrapper);
}
