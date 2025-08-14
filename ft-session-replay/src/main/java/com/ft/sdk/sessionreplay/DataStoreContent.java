package com.ft.sdk.sessionreplay;

public class DataStoreContent<T> {

    private final int versionCode;
    private final T data;

    public DataStoreContent(int versionCode, T data) {
        this.versionCode = versionCode;
        this.data = data;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public T getData() {
        return data;
    }
}
