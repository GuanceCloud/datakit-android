package com.ft.sdk.garble.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class OPData {
    private String op;
    private String content;


    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toJsonString() {
        JSONObject json = new JSONObject();
        try {
            json.put("op", op);
            json.put("opdata", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();

    }


}
