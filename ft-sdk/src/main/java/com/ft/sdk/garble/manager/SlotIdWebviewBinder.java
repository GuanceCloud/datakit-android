package com.ft.sdk.garble.manager;

import com.ft.sdk.garble.utils.LogUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the mapping relationship between slotId and viewId
 * Used to establish associations between WebView and Native View
 *
 * @author Brandon
 */
public class SlotIdWebviewBinder {
    private static final String LOG_TAG = "SlotIdWebviewBinder";

    private static volatile SlotIdWebviewBinder instance;

    /**
     * Mapping of slotId (String) -> ViewBindingInfo (contains viewId and callback)
     */
    private final ConcurrentHashMap<String, ViewBindingInfo> slotIdToViewIdMap = new ConcurrentHashMap<>();
    
    /**
     * Maximum size limit for slotIdToViewIdMap
     */
    private static final int MAX_MAP_SIZE = 20;

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
     * Internal class to store viewId and callback together
     */
    public static class ViewBindingInfo {
        private final String viewId;
        private final BindViewChangeCallBack callback;

        public ViewBindingInfo(String viewId, BindViewChangeCallBack callback) {
            this.viewId = viewId;
            this.callback = callback;
        }

        public String getViewId() {
            return viewId;
        }

        public BindViewChangeCallBack getCallback() {
            return callback;
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

    private SlotIdWebviewBinder() {
    }

    /**
     * Get singleton instance
     *
     * @return SlotIdWebviewBinder instance
     */
    public static SlotIdWebviewBinder get() {
        if (instance == null) {
            synchronized (SlotIdWebviewBinder.class) {
                if (instance == null) {
                    instance = new SlotIdWebviewBinder();
                }
            }
        }
        return instance;
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
                    LogUtils.d(LOG_TAG, "ViewId changed for slotId: " + slotId + ", old viewId: " + existingInfo.getViewId() + ", new viewId: " + viewId + ", notifying callback");
                    callbackToNotify.onViewChanged(viewId);
                }
            }

            // Check if map size limit is reached and remove oldest entry if needed
            // If slotId already exists, we're updating it, so no need to remove
            if (existingInfo == null && slotIdToViewIdMap.size() >= MAX_MAP_SIZE) {
                // Remove the first entry found to make room for new entry
                String firstKey = slotIdToViewIdMap.keySet().iterator().next();
                slotIdToViewIdMap.remove(firstKey);
                LogUtils.d(LOG_TAG, "Map size limit reached (" + MAX_MAP_SIZE + "), removed oldest entry: " + firstKey);
            }

            // Store new binding info
            ViewBindingInfo newInfo = new ViewBindingInfo(viewId, callback);
            slotIdToViewIdMap.put(slotId, newInfo);
            LogUtils.d(LOG_TAG, "Bind slotId: " + slotId + " -> viewId: " + viewId + (callback != null ? " with callback" : ""));
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
     * Clear all binding relationships
     */
    public void clear() {
        slotIdToViewIdMap.clear();
        LogUtils.d(LOG_TAG, "Clear all bindings");
    }
}
