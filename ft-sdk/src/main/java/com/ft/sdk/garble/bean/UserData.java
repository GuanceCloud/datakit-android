package com.ft.sdk.garble.bean;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * BY huangDianHua
 * DATE:2020-01-06 18:13
 * Description:
 */
public class UserData {
    private String sessionId;
    private String name;
    private String id;
    private JSONObject exts;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

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

    public JSONObject getExts() {
        return exts;
    }

    public void setExts(JSONObject exts) {
        this.exts = exts;
    }

    public String createDBDataString(){
        JSONObject data = new JSONObject();
        try {
            data.put("name",name);
            data.put("id",id);
            data.put("exts",exts);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data.toString();
    }

    public void parseUserDataFromDBData(String data){
        try{
            JSONObject jsonObject = new JSONObject(data);
            name = jsonObject.optString("name");
            id = jsonObject.optString("id");
            exts = jsonObject.optJSONObject("exts");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isEmpty(){
        return Utils.isNullOrEmpty(name) || Utils.isNullOrEmpty(id) || exts == null;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof UserData){
            UserData temp = (UserData) obj;
            if(sessionId == null || temp.sessionId == null || !sessionId.equals(temp.sessionId)){
                if(!(sessionId == null && temp.sessionId == null)) {
                    return false;
                }
            }

            if(id == null || temp.id == null || !id.equals(temp.id)){
                if(!(id == null && temp.id == null)) {
                    return false;
                }
            }

            if(name == null || temp.name == null || !name.equals(temp.name)){
                if(!(name == null && temp.name == null)) {
                    return false;
                }
            }

            if(exts == null || temp.exts == null || !exts.toString().equals(temp.exts.toString())){
                if(!(exts == null && temp.exts == null)) {
                    return false;
                }
            }
            return true;
        }else{
            return false;
        }
    }
}
