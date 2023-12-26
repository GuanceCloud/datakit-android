package com.ft.sdk.garble.bean;

import android.util.Log;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * BY huangDianHua
 * DATE:2020-01-06 18:13
 * Description:用户绑定存储数据
 */
public class UserData {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "UserData";

    /**
     * 用户名称
     */
    private String name;
    /**
     * 用户 ID
     */
    private String id;
    /**
     * 用户 email
     */
    private String email;

    /**
     * 设置数据扩展 key value 方式添加
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
     * 通过 json string 转化 {@link #exts} 数据
     *
     * @param data
     */
    public void setExtsWithJsonString(String data) {
        try {
            Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            exts = new Gson().fromJson(data, type);
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));
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
