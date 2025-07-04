package com.ft.sdk.garble.bean;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.manager.SingletonGson;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * BY huangDianHua
 * DATE:2020-01-06 18:13
 * Description: User binding storage data
 */
public class UserData {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "UserData";

    /**
     * User name
     */
    private String name;
    /**
     * User ID
     */
    private String id;
    /**
     * User email
     */
    private String email;

    /**
     * Set data extension key-value pairs
     */
    private HashMap<String, String> exts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, String> getExts() {
        return exts;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setExts(HashMap<String, String> exts) {
        this.exts = exts;
    }

    /**
     * Convert {@link #exts} data from json string
     *
     * @param data json string data content
     */
    public void setExtsWithJsonString(String data) {
        try {
            Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            exts = SingletonGson.getInstance().fromJson(data, type);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof UserData) {
            UserData temp = (UserData) obj;
            if (id == null || temp.id == null || !id.equals(temp.id)) {
                if (!(id == null && temp.id == null)) {
                    return false;
                }
            }

            if (name == null || temp.name == null || !name.equals(temp.name)) {
                if (!(name == null && temp.name == null)) {
                    return false;
                }
            }

            if (exts == null || temp.exts == null || !exts.toString().equals(temp.exts.toString())) {
                return exts == null && temp.exts == null;
            }
            return true;
        } else {
            return false;
        }
    }
}
