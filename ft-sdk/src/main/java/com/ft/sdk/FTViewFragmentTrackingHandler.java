package com.ft.sdk;

/**
 * Fragment view tracking handler interface
 * 
 * This interface allows you to customize how Fragment views are tracked in RUM data.
 * When a Fragment is created, resumed, or stopped, this handler will be called to determine
 * how the Fragment should be tracked.
 * 
 * <p>Usage example:</p>
 * <pre>{@code
 * FTSdk.initRUMWithConfig(new FTRUMConfig()
 *     .setViewFragmentTrackingHandler(new FTViewFragmentTrackingHandler() {
 *         @Override
 *         public HandlerView isInTake(FragmentWrapper fragment) {
 *             String fragmentName = fragment.getSimpleClassName();
 *             
 *             // Customize view name based on fragment type
 *             if (fragmentName.equals("HomeFragment")) {
 *                 HashMap<String, Object> properties = new HashMap<>();
 *                 properties.put("fragment_type", "home");
 *                 return new HandlerView("Custom_Home_View", properties);
 *             } else if (fragmentName.startsWith("Detail")) {
 *                 return new HandlerView("Custom_Detail_View");
 *             }
 *             
 *             // Return null to skip tracking for this fragment
 *             return null;
 *         }
 *     })
 * );
 * }</pre>
 * 
 * @see FTRUMConfig#setViewFragmentTrackingHandler(FTViewFragmentTrackingHandler)
 * @see HandlerView
 * @see FragmentWrapper
 */
public interface FTViewFragmentTrackingHandler {

    /**
     * Determine how a Fragment should be tracked
     * 
     * This method is called when a Fragment lifecycle event occurs (create, resume, stop).
     * You can customize the view name and add custom properties for the Fragment.
     * 
     * @param fragment The Fragment wrapper containing information about the Fragment
     * @return HandlerView with custom view name and properties, or null to skip tracking
     * 
     * @see HandlerView
     * @see FragmentWrapper#getSimpleClassName()
     * @see FragmentWrapper#getClassName()
     */
    HandlerView isInTake(FragmentWrapper fragment);
}
