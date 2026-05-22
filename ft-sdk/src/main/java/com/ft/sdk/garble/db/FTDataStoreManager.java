package com.ft.sdk.garble.db;

import android.content.Context;
import android.content.pm.ProviderInfo;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.db.file.FTFileDataStore;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

/**
 * Entry point for SDK persistence. SQLite-backed storage remains the default
 * for smooth upgrades, while file-backed storage is available as an opt-in path.
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
                    store = createDbDataStoreOrFileFallback(false);
                    dataStore = store;
                }
            }
        }
        return store;
    }

    public static void init(FTSDKConfig config) {
        synchronized (FTDataStoreManager.class) {
            boolean migrateDbFlatCache = config != null && config.isNeedTransformOldCache();
            if (config != null && config.isFileDataStoreShadow()) {
                if (isDbProviderAvailable()) {
                    dataStore = new FTShadowDataStore(FTDBManager.get(),
                            new FTFileDataStore(FTApplication.getApplication()));
                } else {
                    LogUtils.e(TAG, "FTContentProvider is not declared. File data store shadow mode is disabled.");
                    dataStore = createFileDataStore(migrateDbFlatCache);
                }
            } else if (config != null && config.isUseFileDataStore()) {
                dataStore = createFileDataStore(migrateDbFlatCache);
            } else {
                dataStore = createDbDataStoreOrFileFallback(migrateDbFlatCache);
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

    private static FTDataStore createDbDataStoreOrFileFallback(boolean migrateDbFlatCache) {
        if (isDbProviderAvailable()) {
            return FTDBManager.get();
        }
        LogUtils.e(TAG, "FTContentProvider is not declared. Falling back to file-backed storage.");
        return createFileDataStore(migrateDbFlatCache);
    }

    private static FTDataStore createFileDataStore(boolean migrateDbFlatCache) {
        boolean canMigrateDbFlatCache = migrateDbFlatCache && isDbProviderAvailable();
        if (migrateDbFlatCache && !canMigrateDbFlatCache) {
            LogUtils.e(TAG, "FTContentProvider is not declared. DB flat cache migration is skipped.");
        }
        return new FTFileDataStore(FTApplication.getApplication(), canMigrateDbFlatCache);
    }
}
