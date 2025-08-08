package com.ft.sdk;

import android.app.Activity;

/**
 * Activity view tracking handler interface
 * 
 * This interface allows you to customize how Activity views are tracked in RUM data.
 * When an Activity is created, resumed, or paused, this handler will be called to determine
 * how the Activity should be tracked.
 **
 * @see FTRUMConfig#setViewActivityTrackingHandler(FTViewActivityTrackingHandler)
 * @see HandlerView
 * @see Activity
 */
public interface FTViewActivityTrackingHandler {

    /**
     * Determine how an Activity should be tracked
     * 
     * This method is called when an Activity lifecycle event occurs (create, resume, pause).
     * You can customize the view name and add custom properties for the Activity.
     * 
     * @param activity The Activity instance to be tracked
     * @return HandlerView with custom view name and properties, or null to skip tracking
     * 
     * @see HandlerView
     * @see Activity#getClass()
     */
    HandlerView isInTake(Activity activity);
}
