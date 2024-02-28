package com.ft.sdk;

import com.ft.sdk.garble.utils.Utils;

import java.io.File;
import java.io.IOException;

public class SyncDataCacheManager {

    private static final String TAG = "SyncDataCacheManager";
    private File cacheFile;


    private static class SingletonHolder {
        private static final SyncDataCacheManager INSTANCE = new SyncDataCacheManager();
    }

    public static SyncDataCacheManager get() {
        return SyncDataCacheManager.SingletonHolder.INSTANCE;
    }

    public SyncDataCacheManager() {

    }

    public void init() {
        File cacheDir = FTApplication.getApplication().getCacheDir();
        // 创建一个新的文件
        cacheFile = new File(cacheDir, "UploadData");
    }

    public synchronized void appendData(String data) {
        try {
            Utils.appendData(cacheFile, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}