package com.ft.sdk;

/**
 * Action tracking handler interface
 * 
 * This interface allows you to customize how user actions are tracked in RUM data.
 * When a user action occurs (click, touch, etc.), this handler will be called to determine
 * how the action should be tracked.
 **
 * @see FTRUMConfig#setActionTrackingHandler(FTActionTrackingHandler)
 * @see HandlerAction
 * @see ActionEventWrapper
 * @see ActionSourceType
 */
public interface FTActionTrackingHandler {

    /**
     * Determine how a user action should be tracked
     * 
     * This method is called when a user action occurs (click, touch, etc.).
     * You can customize the action name and add custom properties for the action.
     * 
     * @param actionEventWrapper The action event wrapper containing information about the action
     * @return HandlerAction with custom action name and properties, or null to skip tracking
     * 
     * @see HandlerAction
     * @see ActionEventWrapper#getSourceType()
     * @see ActionEventWrapper#getSource()
     * @see ActionEventWrapper#getExtra()
     */
    HandlerAction isInTake(ActionEventWrapper actionEventWrapper);
}
