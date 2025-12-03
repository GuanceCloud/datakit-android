package com.ft.sdk.sessionreplay;

import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.NoOpInternalLogger;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the mapping relationship between slotId and viewId
 * Used to establish associations between WebView and Native View
 *
 * @author Brandon
 */
public class SlotIdWebviewBinder {
    private static final String LOG_TAG = "SlotIdWebviewBinder";

    /**
     * Mapping of slotId (String) -> ViewBindingInfo (contains viewId and callback)
     */
    private final ConcurrentHashMap<String, ViewBindingInfo> slotIdToViewIdMap = new ConcurrentHashMap<>();

    /**
     * Latest slotId that was bound
     * Used to track the most recent slotId globally
     */
    private volatile long latestSlotId = 0;

    /**
     * Maximum size limit for slotIdToViewIdMap
     */
    private static final int MAX_MAP_SIZE = 20;

    /**
     * Flag to track if there are any active ViewBindingInfo entries
     * Used to optimize setAllInactive() for frequent calls
     */
    private volatile boolean hasActiveEntries = false;

    private final InternalLogger logger;

    /**
     * Constructor
     *
     * @param logger Internal logger instance
     */
    public SlotIdWebviewBinder(InternalLogger logger) {
        this.logger = logger != null ? logger : new NoOpInternalLogger();
    }

    /**
     * Callback interface for notifying view changes
     */
    public interface BindViewChangeCallBack {
        /**
         * Called when a new slotId is bound to an existing viewId
         *
         * @param newViewId The viewId that was already bound
         */
        void onViewChanged(String newViewId);
    }

    /**
     * Callback interface for notifying slotId rebind
     */
    public interface SlotRebindCallBack {
        /**
         * Called when the same slotId is rebound
         *
         * @param slotId The slotId that was rebound
         */
        void onSlotRebound(long slotId);
    }

    /**
     * Internal class to store viewId and callback together
     */
    public static class ViewBindingInfo {
        private final String viewId;
        private final BindViewChangeCallBack callback;
        private SlotRebindCallBack slotRebindCallback;
        private volatile boolean isActive = true;

        public ViewBindingInfo(String viewId, BindViewChangeCallBack callback) {
            this.viewId = viewId;
            this.callback = callback;
            this.isActive = true;
        }

        public String getViewId() {
            return viewId;
        }

        public BindViewChangeCallBack getCallback() {
            return callback;
        }

        public SlotRebindCallBack getSlotRebindCallback() {
            return slotRebindCallback;
        }

        public void setSlotRebindCallback(SlotRebindCallBack slotRebindCallback) {
            this.slotRebindCallback = slotRebindCallback;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            this.isActive = active;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ViewBindingInfo that = (ViewBindingInfo) o;
            if (viewId != null ? !viewId.equals(that.viewId) : that.viewId != null) return false;
            return callback != null ? callback.equals(that.callback) : that.callback == null;
        }

        @Override
        public int hashCode() {
            int result = viewId != null ? viewId.hashCode() : 0;
            result = 31 * result + (callback != null ? callback.hashCode() : 0);
            return result;
        }
    }



    /**
     * Bind slotId, viewId and callback
     *
     * @param slotId   WebView's slotId (System.identityHashCode)
     * @param viewId   Native View's viewId
     * @param callback Callback to be called when viewId changes
     */
    public void bind(String slotId, String viewId, BindViewChangeCallBack callback) {
        if (slotId != null && viewId != null) {
            // Get existing binding info for this slotId
            ViewBindingInfo existingInfo = slotIdToViewIdMap.get(slotId);
            boolean isRebind = existingInfo != null;

            // Check if viewId changed
            boolean viewIdChanged = false;
            if (existingInfo != null) {
                String oldViewId = existingInfo.getViewId();
                if (!viewId.equals(oldViewId)) {
                    viewIdChanged = true;
                }
            }

            // If viewId changed and callback is not null, notify callback
            if (viewIdChanged) {
                // Use existing callback if available, otherwise use new callback
                BindViewChangeCallBack callbackToNotify = existingInfo != null && existingInfo.getCallback() != null
                        ? existingInfo.getCallback()
                        : callback;

                if (callbackToNotify != null) {
                    //LogUtils.d(LOG_TAG, "ViewId changed for slotId: " + slotId + ", old viewId: " + existingInfo.getViewId() + ", new viewId: " + viewId + ", notifying callback");
                    callbackToNotify.onViewChanged(viewId);
                }
            }

            // Check if map size limit is reached and remove oldest entry if needed
            // If slotId already exists, we're updating it, so no need to remove
            if (existingInfo == null && slotIdToViewIdMap.size() >= MAX_MAP_SIZE) {
                // Remove the first entry found to make room for new entry
                String firstKey = slotIdToViewIdMap.keySet().iterator().next();
                slotIdToViewIdMap.remove(firstKey);
                //LogUtils.d(LOG_TAG, "Map size limit reached (" + MAX_MAP_SIZE + "), removed oldest entry: " + firstKey);
            }

            // Store new binding info
            ViewBindingInfo newInfo = new ViewBindingInfo(viewId, callback);
            // Preserve slotRebindCallback if rebinding the same slotId
            if (isRebind && existingInfo != null && existingInfo.getSlotRebindCallback() != null) {
                newInfo.setSlotRebindCallback(existingInfo.getSlotRebindCallback());
            }
            slotIdToViewIdMap.put(slotId, newInfo);
            // New entries are active by default
            hasActiveEntries = true;

            // If rebinding the same slotId and was inactive, notify callback and set active
            if (isRebind && existingInfo != null && !existingInfo.isActive()) {
                // Set active status to true
                newInfo.setActive(true);
                hasActiveEntries = true;
                logger.d(LOG_TAG, "Track slotId:" + slotId + ",rebind,setActive:true");

                // Notify slotRebindCallback if exists
                SlotRebindCallBack slotRebindCallback = newInfo.getSlotRebindCallback();
                if (slotRebindCallback != null) {
                    try {
                        long slotIdLong = Long.parseLong(slotId);
                        slotRebindCallback.onSlotRebound(slotIdLong);
                    } catch (NumberFormatException e) {
                        // Ignore if slotId cannot be parsed
                    }
                }
            }

            // Update latest slotId globally
            try {
                long slotIdLong = Long.parseLong(slotId);
                latestSlotId = slotIdLong;
            } catch (NumberFormatException e) {
                // Ignore if slotId cannot be parsed
            }
        }
    }

    /**
     * Bind slotId and viewId (using long type slotId)
     *
     * @param slotId WebView's slotId (System.identityHashCode)
     * @param viewId Native View's viewId
     */
    public void bind(long slotId, String viewId) {
        bind(String.valueOf(slotId), viewId, null);
    }

    /**
     * Bind slotId, viewId and callback (using long type slotId)
     *
     * @param slotId   WebView's slotId (System.identityHashCode)
     * @param viewId   Native View's viewId
     * @param callback Callback to be called when viewId changes
     */
    public void bind(long slotId, String viewId, BindViewChangeCallBack callback) {
        bind(String.valueOf(slotId), viewId, callback);
    }

    /**
     * Set slot rebind callback for a slotId
     *
     * @param slotId   WebView's slotId
     * @param callback Callback to be called when slotId is rebound
     */
    public void setSlotRebindCallback(String slotId, SlotRebindCallBack callback) {
        if (slotId != null) {
            ViewBindingInfo info = slotIdToViewIdMap.get(slotId);
            if (info != null) {
                info.setSlotRebindCallback(callback);
            }
        }
    }

    /**
     * Set slot rebind callback for a slotId (using long type slotId)
     *
     * @param slotId   WebView's slotId
     * @param callback Callback to be called when slotId is rebound
     */
    public void setSlotRebindCallback(long slotId, SlotRebindCallBack callback) {
        setSlotRebindCallback(String.valueOf(slotId), callback);
    }

    /**
     * Get the corresponding viewId by slotId
     *
     * @param slotId WebView's slotId
     * @return The corresponding viewId, or null if not found
     */
    public String getViewId(String slotId) {
        if (slotId == null) {
            return null;
        }
        ViewBindingInfo info = slotIdToViewIdMap.get(slotId);
        return info != null ? info.getViewId() : null;
    }

    /**
     * Get the corresponding viewId by slotId (using long type slotId)
     *
     * @param slotId WebView's slotId
     * @return The corresponding viewId, or null if not found
     */
    public String getViewId(long slotId) {
        return getViewId(String.valueOf(slotId));
    }

    /**
     * Set the active status for a slotId
     *
     * @param slotId   WebView's slotId
     * @param isActive Whether the WebView is active
     */
    public void setActive(String slotId, boolean isActive) {
        if (slotId != null) {
            ViewBindingInfo info = slotIdToViewIdMap.get(slotId);
            if (info != null) {
                info.setActive(isActive);
                // Update hasActiveEntries flag
                if (isActive) {
                    hasActiveEntries = true;
                } else {
                    // Check if there are any active entries remaining
                    updateHasActiveEntries();
                }
                logger.d(LOG_TAG, "Track slotId:" + slotId + ",setActive:" + isActive);
            }
        }
    }

    /**
     * Set the active status for a slotId (using long type slotId)
     *
     * @param slotId   WebView's slotId
     * @param isActive Whether the WebView is active
     */
    public void setActive(long slotId, boolean isActive) {
        setActive(String.valueOf(slotId), isActive);
    }

    /**
     * Get the active status for a slotId
     *
     * @param slotId WebView's slotId
     * @return true if WebView is active, false otherwise. Returns true if not found (default)
     */
    public boolean isActive(String slotId) {
        if (slotId != null) {
            ViewBindingInfo info = slotIdToViewIdMap.get(slotId);
            if (info != null) {
                return info.isActive();
            }
        }
        return true; // Default to true if not found
    }

    /**
     * Get the active status for a slotId (using long type slotId)
     *
     * @param slotId WebView's slotId
     * @return true if WebView is active, false otherwise. Returns true if not found (default)
     */
    public boolean isActive(long slotId) {
        return isActive(String.valueOf(slotId));
    }

    /**
     * Get the latest slotId that was bound
     *
     * @return The latest slotId, or 0 if no slotId has been bound
     */
    public long getLatestSlotId() {
        return latestSlotId;
    }

    /**
     * Set all ViewBindingInfo isActive to false
     * Optimized for frequent calls: skips iteration if no active entries exist
     */
    public void setAllInactive() {
        if (slotIdToViewIdMap.isEmpty() || !hasActiveEntries) {
            return;
        }
        boolean foundActive = false;
        for (ViewBindingInfo info : slotIdToViewIdMap.values()) {
            if (info != null && info.isActive()) {
                info.setActive(false);
                foundActive = true;
            }
        }
        // Update flag after iteration
        hasActiveEntries = foundActive;
    }

    /**
     * Update hasActiveEntries flag by checking all entries
     * Called when an entry is set to inactive to determine if any active entries remain
     */
    private void updateHasActiveEntries() {
        if (slotIdToViewIdMap.isEmpty()) {
            hasActiveEntries = false;
            return;
        }
        for (ViewBindingInfo info : slotIdToViewIdMap.values()) {
            if (info != null && info.isActive()) {
                hasActiveEntries = true;
                return;
            }
        }
        hasActiveEntries = false;
    }

    /**
     * Clear all binding relationships
     */
    public void clear() {
        slotIdToViewIdMap.clear();
        latestSlotId = 0;
        hasActiveEntries = false;
    }
}
