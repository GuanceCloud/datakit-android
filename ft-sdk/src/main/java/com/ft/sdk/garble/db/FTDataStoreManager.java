package com.ft.sdk.garble.db;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.db.file.FTFileDataStore;

/**
 * Entry point for SDK persistence. The current default implementation is the
 * SQLite-backed FTDBManager, while future implementations can be swapped here.
 */
public class FTDataStoreManager {
    private static volatile FTDataStore dataStore;

    private FTDataStoreManager() {
    }

    public static FTDataStore get() {
        FTDataStore store = dataStore;
        if (store == null) {
            synchronized (FTDataStoreManager.class) {
                store = dataStore;
                if (store == null) {
                    store = FTDBManager.get();
                    dataStore = store;
                }
            }
        }
        return store;
    }

    public static void init(FTSDKConfig config) {
        synchronized (FTDataStoreManager.class) {
            if (config != null && config.isUseFileDataStore()) {
                dataStore = new FTFileDataStore(FTApplication.getApplication());
            } else if (config != null && config.isFileDataStoreShadow()) {
                dataStore = new FTShadowDataStore(FTDBManager.get(),
                        new FTFileDataStore(FTApplication.getApplication()));
            } else {
                dataStore = FTDBManager.get();
            }
        }
    }

    static void setDataStore(FTDataStore store) {
        synchronized (FTDataStoreManager.class) {
            dataStore = store;
        }
    }

    public static void refreshFileSizeCache() {
        FTDataStore store = get();
        if (store instanceof FTFileDataStore) {
            ((FTFileDataStore) store).refreshFileSizeCache();
        }
    }

    public static void release() {
        synchronized (FTDataStoreManager.class) {
            dataStore = null;
        }
    }
}
