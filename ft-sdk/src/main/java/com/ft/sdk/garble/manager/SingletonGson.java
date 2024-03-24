package com.ft.sdk.garble.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 统一管理全局的 Gson 对象, 开启 disableHtmlEscaping ，serializeNulls
 */
public class SingletonGson {
    private static volatile Gson instance;

    private SingletonGson() {
    }

    public static Gson getInstance() {
        if (instance == null) {
            synchronized (SingletonGson.class) {
                if (instance == null) {
                    instance = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
                }
            }
        }
        return instance;
    }
}