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
     * 添加 Action
     *
     * @param actionName action 名称
     * @param actionType action 类型
     * @param duration   纳秒，持续时间
     */
    public void addAction(String actionName, String actionType, long duration) {
        if (innerManager != null) {
            innerManager.addAction(actionName, actionType, duration, Utils.getCurrentNanoTime());
        }
    }

    /**
     * 添加 action
     *
     * @param actionName action 名称
     * @param actionType action 类型
     */
    public void startAction(String actionName, String actionType) {
        if (innerManager != null) {
            innerManager.startAction(actionName, actionType);
        }
    }

    /**
     * 添加 action
     *
     * @param actionName action 名称
     * @param actionType action 类型
     * @param property   附加属性参数
     */
    public void startAction(String actionName, String actionType, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.startAction(actionName, actionType, property);
        }
    }


    /**
     * resource 起始
     *
     * @param resourceId 资源 Id
     */
    public void startResource(String resourceId) {
        if (innerManager != null) {
            innerManager.startResource(resourceId);
        }
    }


    /**
     * resource 起始
     *
     * @param resourceId 资源 Id
     * @param property   附加属性参数
     */
    public void startResource(String resourceId, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.startResource(resourceId, property);
        }
    }


    /**
     * resource 终止
     *
     * @param resourceId 资源 Id
     */
    public void stopResource(String resourceId) {
        if (innerManager != null) {
            innerManager.stopResource(resourceId);
        }
    }


    /**
     * @param resourceId 资源 Id
     * @param property   附加属性参数
     */
    public void stopResource(final String resourceId, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.stopResource(resourceId, property);
        }
    }


    /**
     * 创建 view
     *
     * @param viewName 界面名称
     * @param loadTime 加载事件，单位毫秒 ms
     */
    public void onCreateView(String viewName, long loadTime) {
        if (innerManager != null) {
            innerManager.onCreateView(viewName, loadTime);
        }
    }


    /**
     * view 起始
     *
     * @param viewName 当前页面名称
     */
    public void startView(String viewName) {
        if (innerManager != null) {
            innerManager.startView(viewName);
        }
    }

    /**
     * view 起始
     *
     * @param viewName 当前页面名称
     * @param property 附加属性参数
     */
    public void startView(String viewName, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.startView(viewName, property);
        }
    }


    /**
     * view 结束
     */
    public void stopView() {
        if (innerManager != null) {
            innerManager.stopView();
        }
    }

    /**
     * view 结束
     *
     * @param property 附加属性参数
     */
    public void stopView(HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.stopView(property);
        }
    }


    /**
     * 添加错误信息
     *
     * @param log       日志
     * @param message   消息
     * @param errorType 错误类型
     * @param state     程序运行状态
     */
    public void addError(String log, String message, ErrorType errorType, AppState state) {
        if (innerManager != null) {
            innerManager.addError(log, message, errorType, state);
        }
    }

    /**
     * 添加错误
     *
     * @param log       日志
     * @param message   消息
     * @param errorType 错误类型
     * @param state     程序运行状态
     * @param dateline  发生时间，纳秒
     */
    public void addError(String log, String message, long dateline, ErrorType errorType,
                         AppState state) {
        if (innerManager != null) {
            innerManager.addError(log, message, dateline, errorType, state);
        }

    }


    /**
     * 添加错误信息
     *
     * @param log       日志
     * @param message   消息
     * @param errorType 错误类型
     * @param state     程序运行状态
     * @param property  附加属性
     */
    public void addError(String log, String message, ErrorType errorType, AppState state, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.addError(log, message, errorType, state, property);
        }
    }


    /**
     * 添加错误
     *
     * @param log       日志
     * @param message   消息
     * @param errorType 错误类型
     * @param state     程序运行状态
     * @param dateline  发生时间，纳秒
     * @param property  附加属性
     */
    public void addError(String log, String message, long dateline, ErrorType errorType,
                         AppState state, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.addError(log, message, dateline, errorType, state, property);
        }

    }

    /**
     * 添加长任务
     *
     * @param log      日志内容
     * @param duration 持续时间，纳秒
     * @param property 附加属性
     */
    public void addLongTask(String log, long duration, HashMap<String, Object> property) {
        if (innerManager != null) {
            innerManager.addLongTask(log, duration, property);
        }
    }


    /**
     * 添加长任务
     *
     * @param log      日志内容
     * @param duration 持续时间，纳秒
     */
    public void addLongTask(String log, long duration) {
        if (innerManager != null) {
            innerManager.addLongTask(log, duration);
        }
    }


    /**
     * 设置网络传输内容
     *
     * @param resourceId    资源 id
     * @param params
     * @param netStatusBean
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


    public void release() {
        innerManager = null;
    }
}
