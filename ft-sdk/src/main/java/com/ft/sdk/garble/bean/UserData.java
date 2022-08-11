package com.ft.sdk.garble.bean;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * BY huangDianHua
 * DATE:2020-01-06 18:13
 * Description:
 */
public class UserData {
    private String name;
    private String id;
    private String email;
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

    public void setExtsWithJsonString(String data) {
        try {
            Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            exts = new Gson().fromJson(data, type);
        } catch (Exception e) {
            e.printStackTrace();
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
                if (!(exts == null && temp.exts == null)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
