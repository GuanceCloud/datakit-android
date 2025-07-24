package com.ft.sdk;


import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;

public class FTRUMGlobalManager {

    private static class SingletonHolder {
        private static final FTRUMGlobalManager INSTANCE = new FTRUMGlobalManager();
    }

    public static FTRUMGlobalManager get() {
        return FTRUMGlobalManager.SingletonHolder.INSTANCE;
    }

    private FTRUMInnerManager innerManager;


    void initConfig(FTRUMConfig config) {
        if (config.isRumEnable()) {
            innerManager = FTRUMInnerManager.get();
        }
    }

    /**
     * Add Action，this type of data cannot be associated with Error, Resource, LongTask data
     *
     * @param actionName action name
     * @param actionType action type
     * @param duration   nanosecond, duration
     * @param property   extended attributes
     */
    public void addAction(String actionName, String actionType, long duration, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.addAction(actionName, actionType, duration, Utils.getCurrentNanoTime(), property);
        }
    }

    /**
     * Add Action, this type of data cannot be associated with Error, Resource, LongTask data
     *
     * @param actionName action name
     * @param actionType action type
     */
    public void addAction(String actionName, String actionType) {
        if (innerManager != null) {
            innerManager.addAction(actionName, actionType, 0, Utils.getCurrentNanoTime(), null);
        }
    }

    /**
     * Add Action, this type of data cannot be associated with Error, Resource, LongTask data
     *
     * @param actionName action name
     * @param actionType action type
     * @param property   extended attributes
     */
    public void addAction(String actionName, String actionType, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.addAction(actionName, actionType, 0, Utils.getCurrentNanoTime(), property);
        }
    }

    /**
     * Add Action, this type of data cannot be associated with Error, Resource, LongTask data
     *
     * @param actionName action name
     * @param actionType action type
     * @param duration   nanosecond, duration
     */
    public void addAction(String actionName, String actionType, long duration) {
        if (innerManager != null) {
            innerManager.addAction(actionName, actionType, duration, Utils.getCurrentNanoTime(), null);
        }
    }

    /**
     * Add action
     * <p>
     * The startAction method includes a duration calculation mechanism and attempts
     * to associate data with nearby Resource, LongTask, and Error events during that period.
     * It has a 100 ms frequency protection mechanism and is recommended for user interaction-type events.
     * If you need to call actions frequently, please use addAction instead. This method does not conflict
     * with startAction and does not associate with the current Resource, LongTask, or Error events.
     *
     * @param actionName action name
     * @param actionType action type
     */
    public void startAction(String actionName, String actionType) {
        if (innerManager != null) {
            innerManager.startAction(actionName, actionType);
        }
    }

    /**
     * Add action
     *
     * @param actionName action name
     * @param actionType action type
     * @param property   additional attribute parameters
     */
    public void startAction(String actionName, String actionType, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.startAction(actionName, actionType, property);
        }
    }


    /**
     * resource start
     *
     * @param resourceId resource Id
     */
    public void startResource(String resourceId) {
        if (innerManager != null) {
            innerManager.startResource(resourceId);
        }
    }


    /**
     * resource start
     *
     * @param resourceId resource Id
     * @param property   additional attribute parameters
     */
    public void startResource(String resourceId, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.startResource(resourceId, property);
        }
    }


    /**
     * resource end
     *
     * @param resourceId resource Id
     */
    public void stopResource(String resourceId) {
        if (innerManager != null) {
            innerManager.stopResource(resourceId);
        }
    }


    /**
     * @param resourceId resource Id
     * @param property   additional attribute parameters
     */
    public void stopResource(final String resourceId, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.stopResource(resourceId, property);
        }
    }


    /**
     * Create view
     *
     * @param viewName view name
     * @param loadTime load time, unit milliseconds ms
     */
    public void onCreateView(String viewName, long loadTime) {
        if (innerManager != null) {
            innerManager.onCreateView(viewName, loadTime);
        }
    }


    /**
     * view start
     *
     * @param viewName current page name
     */
    public void startView(String viewName) {
        if (innerManager != null) {
            innerManager.startView(viewName);
        }
    }

    /**
     * view start
     *
     * @param viewName current page name
     * @param property additional attribute parameters
     */
    public void startView(String viewName, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.startView(viewName, property);
        }
    }


    /**
     * view end
     */
    public void stopView() {
        if (innerManager != null) {
            innerManager.stopView();
        }
    }

    /**
     * view end
     *
     * @param property additional attribute parameters
     */
    public void stopView(HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.stopView(property, null);
        }
    }

    /**
     * Add error information, default AppState.RUN
     *
     * @param log       log
     * @param message   message
     * @param errorType error type
     */
    public void addError(String log, String message, String errorType) {
        if (innerManager != null) {
            innerManager.addError(log, message, errorType, AppState.RUN);
        }
    }

    /**
     * Add error information
     *
     * @param log       log
     * @param message   message
     * @param errorType error type
     * @param state     program running state
     */
    public void addError(String log, String message, String errorType, AppState state) {
        if (innerManager != null) {
            innerManager.addError(log, message, errorType, state);
        }
    }

    /**
     * Add error
     *
     * @param log       log
     * @param message   message
     * @param errorType error type
     * @param state     program running state
     * @param dateline  occurrence time, nanosecond
     */
    public void addError(String log, String message, long dateline, String errorType,
                         AppState state) {
        if (innerManager != null) {
            innerManager.addError(log, message, dateline, errorType, state, null);
        }

    }


    /**
     * Add error information
     *
     * @param log       log
     * @param message   message
     * @param errorType error type
     * @param state     program running state
     * @param property  additional attribute parameters
     */
    public void addError(String log, String message, String errorType, AppState state, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.addError(log, message, errorType, state, property);
        }
    }


    /**
     * Add error
     *
     * @param log       log
     * @param message   message
     * @param errorType error type
     * @param state     program running state
     * @param dateline  occurrence time, nanosecond
     * @param property  additional attribute parameters
     */
    public void addError(String log, String message, long dateline, String errorType,
                         AppState state, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.addError(log, message, dateline, errorType, state, property, null);
        }

    }


    /**
     * Add error information
     *
     * @param log       log
     * @param message   message
     * @param errorType error type
     * @param state     program running state
     */
    public void addError(String log, String message, ErrorType errorType, AppState state) {
        if (innerManager != null) {
            innerManager.addError(log, message, errorType.toString(), state);
        }
    }

    /**
     * Add error
     *
     * @param log       log
     * @param message   message
     * @param errorType error type
     * @param state     program running state
     * @param dateline  occurrence time, nanosecond
     */
    public void addError(String log, String message, long dateline, ErrorType errorType,
                         AppState state) {
        if (innerManager != null) {
            innerManager.addError(log, message, dateline, errorType.toString(), state, null);
        }

    }


    /**
     * Add error information
     *
     * @param log       log
     * @param message   message
     * @param errorType error type
     * @param state     program running state
     * @param property  additional attribute parameters
     */
    public void addError(String log, String message, ErrorType errorType, AppState state, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.addError(log, message, errorType.toString(), state, property);
        }
    }


    /**
     * Add error
     *
     * @param log       log
     * @param message   message
     * @param errorType error type
     * @param state     program running state
     * @param dateline  occurrence time, nanosecond
     * @param property  additional attribute parameters
     */
    public void addError(String log, String message, long dateline, ErrorType errorType,
                         AppState state, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.addError(log, message, dateline, errorType.toString(), state, property, null);
        }

    }

    /**
     * Add long task
     *
     * @param log      log
     * @param duration duration, nanosecond
     * @param property additional attribute parameters
     */
    public void addLongTask(String log, long duration, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.addLongTask(log, duration, property);
        }
    }


    /**
     * Add long task
     *
     * @param log      log
     * @param duration duration, nanosecond
     */
    public void addLongTask(String log, long duration) {
        if (innerManager != null) {
            innerManager.addLongTask(log, duration);
        }
    }


    /**
     * Set network transmission content
     *
     * @param resourceId    resource id
     * @param params        {@link ResourceParams} request content information
     * @param netStatusBean {@link  NetStatusBean} network performance indicators
     */
    public void addResource(String resourceId, ResourceParams params, NetStatusBean netStatusBean) {
        if (innerManager != null) {
            if (Utils.isNullOrEmpty(params.requestHeader)) {
                params.requestHeader = Utils.convertToHttpRawData(params.requestHeaderMap);
            }
            if (Utils.isNullOrEmpty(params.responseHeader)) {
                params.responseHeader = Utils.convertToHttpRawData(params.responseHeaderMap);
            }

            if (params.responseContentLength <= 0) {
                if (params.responseBody == null) {
                    params.responseBody = "";
                }
                params.responseContentLength = params.responseBody.length() + params.responseHeader.length();
            }

            innerManager.addResource(resourceId, params, netStatusBean);
        }
    }


    /**
     * Object release, clear to zero after SDK shutdown
     */
    public void release() {
        innerManager = null;
    }
}
