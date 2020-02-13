package com.ft.sdk.garble;

import android.content.SharedPreferences;

import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * BY huangDianHua
 * DATE:2020-01-07 10:18
 * Description:
 */
public class FTUserConfig {
    private static volatile FTUserConfig instance;
    private volatile String sessionId;
    private List<UserData> userDataList;
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
     *
     * @return
     */
    public boolean isUserDataBinded() {
        return userDataBinded;
    }

    /**
     * 是否需要绑定用户信息
     *
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
    public void createNewSessionId() {
        sessionId = UUID.randomUUID().toString();
        SharedPreferences sp = Utils.getSharedPreferences(FTSdk.get().getApplication());
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
        SharedPreferences sp = Utils.getSharedPreferences(FTSdk.get().getApplication());
        sessionId = sp.getString(Constants.FT_USER_SESSION_ID, null);
        return sessionId;
    }

    /**
     * 清除用户的SessionId
     */
    public void clearSessionId() {
        SharedPreferences sp = Utils.getSharedPreferences(FTSdk.get().getApplication());
        sp.edit().remove(Constants.FT_USER_SESSION_ID).apply();
        sessionId = null;
    }

    /**
     * 从数据库中查询用户信息
     */
    public void initUserDataFromDB() {
        ThreadPoolUtils.get().execute(() -> {
            userDataList = FTDBManager.get().queryFTUserDataList();
            if (getUserData(sessionId) != null) {
                userDataBinded = true;
            }
        });
    }

    /**
     * 根据 sessionID 获取用户信息
     * @param sessionId
     * @return
     */
    public UserData getUserData(String sessionId) {
        if (userDataList != null) {
            for (UserData userData : userDataList) {
                if (sessionId != null && sessionId.equals(userData.getSessionId())) {
                    return userData;
                }
            }
        }
        return null;
    }

    /**
     * 判断当前缓存的 sessionId 是否有绑定用户信息
     * @return
     */
    public boolean currentSessionHasUser(){
        if(sessionId != null) {
            return getUserData(sessionId) != null;
        }else{
            return false;
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
        ThreadPoolUtils.get().execute(() -> {
            try {
                UserData userData = new UserData();
                userData.setSessionId(sessionId);
                userData.setName(name);
                userData.setId(id);
                userData.setExts(exts);
                if (userDataList == null || userDataList.isEmpty()) {
                    FTDBManager.get().insertFTUserData(userData);
                    userDataList = new ArrayList<>();
                    userDataList.add(userData);
                } else{
                    int i = 0;
                    for (UserData temp:userDataList) {
                        if(userData.equals(temp)){
                            break;
                        }
                        i++;
                    }
                    if(i == userDataList.size()){
                        FTDBManager.get().insertFTUserData(userData);
                        userDataList.add(userData);
                    }
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
    public void unbindUserData() {
        if (sessionId != null) {
            userDataBinded = false;
        }
    }


}
