package com.ft.sdk;

import com.ft.sdk.garble.utils.Constants;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Link tracing
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
     * Get trace HTTP request header parameters
     *
     * @param key       resource ID
     * @param urlString URL address
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
     * Get trace HTTP request header parameters
     *
     * @param urlString URL address
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
//     * Send trace data
//     *
//     * @param key             resource ID
//     * @param httpMethod      Request type post, get
//     * @param requestHeader   Request data header
//     * @param responseHeader  Data response header
//     * @param statusCode      HTTP status code
//     * @param errorMsg        Request error message
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
     * Remove by addResource based on key value
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
     * Remove by stopResource based on key value
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
     * Check if release is needed
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
     * Check end lifecycle container, when trace stopResource addResource are both called, the object will be removed
     */
    static class FTTraceManagerContainer {
        private boolean addResourced = !FTRUMConfigManager.get().isRumEnable();
        private boolean resourceStop = addResourced;
        /**
         * Start time
         */
        private final long startTime = System.currentTimeMillis();
        private static final int TIME_OUT = 60000;//Temporarily not considering long connection situations

        private final FTTraceInterceptor.TraceRUMLinkable handler;

        public FTTraceManagerContainer(FTTraceInterceptor.TraceRUMLinkable handler) {
            this.handler = handler;
        }

        /**
         * To avoid memory overflow caused by incorrect calls
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
