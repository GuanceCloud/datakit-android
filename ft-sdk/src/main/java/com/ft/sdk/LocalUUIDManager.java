package com.ft.sdk;

import android.content.SharedPreferences;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

/**
 * Generate random characters in format ft.rd_[uuid 32], will regenerate when app is deleted
 */
public class LocalUUIDManager {
    private static class SingletonHolder {
        private static final LocalUUIDManager INSTANCE = new LocalUUIDManager();
    }

    public static LocalUUIDManager get() {
        return LocalUUIDManager.SingletonHolder.INSTANCE;
    }

    private volatile String randomUUID;


    /**
     * Initialize random userid
     */
    void initRandomUUID() {
        if (Utils.isNullOrEmpty(getRandomUUID())) {
            createNewRandomUUID();
        }
    }

    private void createNewRandomUUID() {
        randomUUID = "ft.rd_" + Utils.randomUUID();
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        sp.edit().putString(Constants.FT_RANDOM_USER_ID, randomUUID).apply();
    }


    /**
     * Get random uuid, if local file cache exists, get from local
     * @return
     */
    String getRandomUUID() {
        if (!Utils.isNullOrEmpty(randomUUID)) {
            return randomUUID;
        }
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        randomUUID = sp.getString(Constants.FT_RANDOM_USER_ID, null);
        return randomUUID;
    }
}
