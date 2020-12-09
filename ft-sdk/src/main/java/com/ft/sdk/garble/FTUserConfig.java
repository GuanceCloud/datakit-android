package com.ft.sdk.garble;

import android.content.SharedPreferences;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

import java.util.UUID;

/**
 * BY huangDianHua
 * DATE:2020-01-07 10:18
 * Description:
 */
public class FTUserConfig {
    private static volatile FTUserConfig instance;
    private volatile String sessionId;
    private UserData mUserData;

    private final Object mLock = new Object();

    private FTUserConfig() {

    }

    public synchronized static FTUserConfig get() {
        if (instance == null) {
            instance = new FTUserConfig();
        }
        return instance;
    }

    /**
     * userDataBinded
     * 用户数据是否已经绑定完成
     *
     * @return
     */
    public boolean isUserDataBinded() {
        return getUserData() != null;
    }

    /**
     * 初始化用户SessionId
     */
    public void initSessionId() {
        if (Utils.isNullOrEmpty(getSessionId())) {
            createNewSessionId();
        }
    }

    /**
     * 创建一个新的用户SessionID
     */
    public void createNewSessionId() {
        sessionId = UUID.randomUUID().toString();
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        sp.edit().putString(Constants.FT_USER_SESSION_ID, sessionId).apply();
    }

    /**
     * 返回用户的SessionId
     *
     * @return
     */
    public String getSessionId() {
        if (!Utils.isNullOrEmpty(sessionId)) {
            return sessionId;
        }
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        sessionId = sp.getString(Constants.FT_USER_SESSION_ID, null);
        return sessionId;
    }


    /**
     * 根据 sessionID 获取用户信息
     *
     * @return
     */
    public UserData getUserData() {
        synchronized (mLock) {
            if (mUserData != null) {
                return mUserData;
            }

            SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
            String id = sp.getString(Constants.FT_USER_USER_ID, null);
            String userName = sp.getString(Constants.FT_USER_USER_NAME, null);
            String ext = sp.getString(Constants.FT_USER_USER_EXT, null);
            if (id == null) {
                return null;
            } else {
                UserData data = new UserData();
                data.setId(id);
                data.setName(userName);
                if (ext != null) {
                    data.setExtsWithJsonString(ext);
                }
                return data;
            }
        }
    }

    /**
     * 绑定用户信息
     *
     * @param name
     * @param id
     * @param exts
     */
    public void bindUserData(String name, String id, JSONObject exts) {
        synchronized (mLock) {
            SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
            sp.edit().putString(Constants.FT_USER_USER_ID, id).apply();
            sp.edit().putString(Constants.FT_USER_USER_NAME, name).apply();
            sp.edit().putString(Constants.FT_USER_USER_EXT, exts != null ? exts.toString() : null).apply();
            UserData data = new UserData();
            data.setId(id);
            data.setName(name);
            data.setExts(exts);
            mUserData = data;
        }
    }

    /**
     * 解绑用户信息
     */
    public void unbindUserData() {
        synchronized (mLock) {
            SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
            sp.edit().remove(Constants.FT_USER_USER_ID).apply();
            sp.edit().remove(Constants.FT_USER_USER_NAME).apply();
            sp.edit().remove(Constants.FT_USER_USER_EXT).apply();
        }
    }

    public static void release() {
        instance = null;
    }

}
