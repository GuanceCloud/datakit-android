package com.ft.sdk.sessionreplay.internal.recorder.resources;

public interface Cache<K, V> {

    void put(V value);

    void put(K element, V value);

    V get(K element);

    int size();

    void clear();

    interface Companion {
    }

    String DOES_NOT_IMPLEMENT_COMPONENTCALLBACKS = "Cache instance does not implement ComponentCallbacks2";

}