package com.ft.sdk;

import android.content.SharedPreferences;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

/**
 * 生成 ft.rd_[uuid 32] 的随机字符，应用删除时会重新生成
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
     * 初始化随机 userid
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
     * 获取随机 uuid 如果本地文件缓存有，则从本地获取
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
