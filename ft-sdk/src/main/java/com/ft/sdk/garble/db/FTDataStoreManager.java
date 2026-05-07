package com.ft.sdk.garble.db;

import android.content.Context;
import android.content.pm.ProviderInfo;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.db.file.FTFileDataStore;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

/**
 * Entry point for SDK persistence. The default implementation is file-backed
 * storage, while SQLite remains available for shadow mode and rollback.
 */
public class FTDataStoreManager {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTDataStoreManager";
    private static volatile FTDataStore dataStore;

    private FTDataStoreManager() {
    }

    public static FTDataStore get() {
        FTDataStore store = dataStore;
        if (store == null) {
            synchronized (FTDataStoreManager.class) {
                store = dataStore;
                if (store == null) {
                    store = new FTFileDataStore(FTApplication.getApplication());
                    dataStore = store;
                }
            }
        }
        return store;
    }

    public static void init(FTSDKConfig config) {
        synchronized (FTDataStoreManager.class) {
            if (config != null && config.isFileDataStoreShadow()) {
                if (isDbProviderAvailable()) {
                    dataStore = new FTShadowDataStore(FTDBManager.get(),
                            new FTFileDataStore(FTApplication.getApplication()));
                } else {
                    LogUtils.e(TAG, "FTContentProvider is not declared. File data store shadow mode is disabled.");
                    dataStore = new FTFileDataStore(FTApplication.getApplication(),
                            config.isNeedTransformOldCache());
                }
            } else if (config == null || config.isUseFileDataStore()) {
                dataStore = new FTFileDataStore(FTApplication.getApplication(),
                        config != null && config.isNeedTransformOldCache());
            } else {
                if (isDbProviderAvailable()) {
                    dataStore = FTDBManager.get();
                } else {
                    LogUtils.e(TAG, "FTContentProvider is not declared. Falling back to file-backed storage.");
                    dataStore = new FTFileDataStore(FTApplication.getApplication(),
                            config.isNeedTransformOldCache());
                }
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

    private static boolean isDbProviderAvailable() {
        Context context = FTApplication.getApplication();
        if (context == null) {
            return false;
        }
        ProviderInfo providerInfo = context.getPackageManager()
                .resolveContentProvider(ProviderHelper.getAuthority(context), 0);
        return providerInfo != null;
    }
}
