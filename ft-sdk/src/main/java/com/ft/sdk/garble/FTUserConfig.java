package com.ft.sdk.garble;

import android.content.SharedPreferences;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
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
    private UserData userData;
    private volatile boolean userDataBinded;
    private volatile boolean needBindUser;

    private FTUserConfig() {

    }

    public synchronized static FTUserConfig get() {
        if (instance == null) {
            instance = new FTUserConfig();
        }
        return instance;
    }

    /**
     * 用户数据是否已经绑定完成
     * @return
     */
    public boolean isUserDataBinded() {
        return userDataBinded;
    }

    /**
     * 是否需要绑定用户信息
     * @return
     */
    public boolean isNeedBindUser() {
        return needBindUser;
    }

    public void setNeedBindUser(boolean needBindUser) {
        this.needBindUser = needBindUser;
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
    private void createNewSessionId() {
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
     * 清除用户的SessionId
     */
    public void clearSessionId() {
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        sp.edit().remove(Constants.FT_USER_SESSION_ID).apply();
        sessionId = null;
    }

    /**
     * 从数据库中查询用户信息
     */
    public void initUserDataFromDB() {
        ThreadPoolUtils.get().execute(() -> {
            userData = FTDBManager.get().queryFTUserData(sessionId);
            if(userData != null && !userData.isEmpty()){
                userDataBinded = true;
            }
        });
    }

    public UserData getUserData() {
        return userData;
    }

    /**
     * 绑定用户信息
     *
     * @param name
     * @param id
     * @param exts
     */
    public void bindUserData(String name, String id, JSONObject exts) {
        ThreadPoolUtils.get().execute(() -> {
            try {
                if (sessionId != null && userData == null || userData.isEmpty() || !(userData.getName().equals(name)
                            && userData.getId().equals(id)
                            && userData.getExts().toString().equals(exts.toString()))) {
                    userData = new UserData();
                    userData.setSessionId(sessionId);
                    userData.setName(name);
                    userData.setId(id);
                    userData.setExts(exts);
                    FTDBManager.get().insertFTUserData(userData);
                }
                userDataBinded = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 解绑用户信息
     */
    public void unbindUserData(){
        if(sessionId != null) {
            ThreadPoolUtils.get().execute(() -> {
                FTDBManager.get().deleteUserData(sessionId);
                userData = null;
                userDataBinded = false;
            });
        }
    }


}
