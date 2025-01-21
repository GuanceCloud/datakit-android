package com.ft.sdk;

import com.ft.sdk.garble.utils.Constants;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 链路追踪
 */
public class FTTraceManager {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTTraceManager";
    private final SizeLimitedConcurrentHashMap<String, FTTraceManagerContainer> handlerMap
            = new SizeLimitedConcurrentHashMap<>(1000);


    private static class SingletonHolder {
        private static final FTTraceManager INSTANCE = new FTTraceManager();
    }

    public static FTTraceManager get() {
        return FTTraceManager.SingletonHolder.INSTANCE;
    }

    /**
     *
     * @param key
     * @param handler
     */
    void putTraceHandler(String key, FTTraceInterceptor.TraceRUMLinkable handler) {
        handlerMap.put(key, new FTTraceManagerContainer(handler));
    }

    /**
     * 获取 trace http 请求头参数
     *
     * @param key       链路 id
     * @param urlString url 地址
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public HashMap<String, String> getTraceHeader(String key, String urlString) {
        FTTraceHandler handler = new FTTraceHandler();
        HashMap<String, String> map = handler.getTraceHeader(urlString);
        putTraceHandler(key, handler);
        return map;
    }

    /**
     * 获取 trace http 请求头参数
     *
     * @param urlString url 地址
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public HashMap<String, String> getTraceHeader(String urlString) {
        return new FTTraceHandler().getTraceHeader(urlString);
    }

    FTTraceInterceptor.TraceRUMLinkable getHandler(String key) {
        FTTraceManagerContainer container = handlerMap.get(key);
        if (container != null) {
            return container.handler;
        }
        return null;
    }


//    /**
//     * 发送 trace 数据
//     *
//     * @param key            链路 id
//     * @param httpMethod     请求类型 post ，get
//     * @param requestHeader  请求数据头
//     * @param responseHeader 数据响应头
//     * @param statusCode     http 状态吗
//     * @param errorMsg       请求错误信息
//     */
//    public void addTrace(String key, String httpMethod, HashMap<String, String> requestHeader,
//                         HashMap<String, String> responseHeader, int statusCode,
//                         String errorMsg) {
//        FTTraceManagerContainer container = handlerMap.get(key);
//        if (container != null) {
//            FTTraceHandler handler = container.handler;
//            String url = handler.getUrl().getHoleUrl();
//            String path = handler.getUrl().getPath();
//            String operationName = httpMethod.toUpperCase() + " " + path;
//            JSONObject json = new JSONObject();
//            try {
//                JSONObject requestContent = new JSONObject();
//                requestContent.put("method", httpMethod);
//                requestContent.put("headers", requestHeader);
//                requestContent.put("url", url);
//
//                JSONObject responseContent = new JSONObject();
//                responseContent.put("code", statusCode);
//                responseContent.put("headers", responseHeader);
//                boolean isError = false;
//                if (statusCode >= 400) {
//                    responseContent.put("error", errorMsg);
//                    isError = true;
//                }
//
//                json.put("requestContent", requestContent);
//                json.put("responseContent", responseContent);
//
//                handler.traceDataUpload(json, operationName, isError);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        removeByTrace(key);
//    }

//    void removeByTrace(String key) {
//        FTTraceManagerContainer container = handlerMap.get(key);
//        if (container != null) {
//            container.traced = true;
//            checkToRemove(key, container);
//        }
//    }

    /**
     * addResource 根据 key 数值移除
     *
     * @param key
     */
    void removeByAddResource(String key) {
        FTTraceManagerContainer container = handlerMap.get(key);
        if (container != null) {
            container.addResourced = true;
            checkToRemove(key, container);
        }
    }

    /**
     * stopResource 根据 key 数值移除
     *
     * @param key
     */
    void removeByStopResource(String key) {
        FTTraceManagerContainer container = handlerMap.get(key);
        if (container != null) {
            container.resourceStop = true;
            checkToRemove(key, container);
        }
    }

    /**
     * 检验是否需要释放
     *
     * @param key
     * @param container
     */
    void checkToRemove(String key, FTTraceManagerContainer container) {
        if (container.addResourced && container.resourceStop || container.isTimeOut()) {
            handlerMap.remove(key);
        }
    }

    /**
     * 检验结束生命周期容器，当 trace stopResource addResource 均调用后，对象会被移除
     */
    static class FTTraceManagerContainer {
        private boolean addResourced = !FTRUMConfigManager.get().isRumEnable();
        private boolean resourceStop = addResourced;
        /**
         * 开始时间
         */
        private final long startTime = System.currentTimeMillis();
        private static final int TIME_OUT = 60000;//暂不考虑长链接情况

        private final FTTraceInterceptor.TraceRUMLinkable handler;

        public FTTraceManagerContainer(FTTraceInterceptor.TraceRUMLinkable handler) {
            this.handler = handler;
        }

        /**
         * 未避免错误调用造成内存溢出
         *
         * @return
         */
        boolean isTimeOut() {
            return System.currentTimeMillis() - startTime > TIME_OUT;
        }
    }

    private static class SizeLimitedConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {
        private final int maxSize;

        public SizeLimitedConcurrentHashMap(int maxSize) {
            super();
            this.maxSize = maxSize;
        }

        @Override
        public V put(K key, V value) {
            V result = super.put(key, value);
            if (size() > maxSize) {
                removeEldest();
            }
            return result;
        }

        private void removeEldest() {
            K eldestKey = null;
            for (K key : keySet()) {
                if (eldestKey == null || key.hashCode() < eldestKey.hashCode()) {
                    eldestKey = key;
                }
            }
            if (eldestKey != null) {
                remove(eldestKey);
            }
        }
    }


}
