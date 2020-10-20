package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.FloatDoubleJsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class OPData {
    private OP op;
    private String content;


    public OP getOp() {
        return op;
    }

    public void setOp(OP op) {
        this.op = op;
    }

    public void setOpFromString(String op) {
        this.op = OP.fromValue(op);
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
            json.put("op", op.value);
            json.put("opdata", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return FloatDoubleJsonUtils.protectValueFormat(json);

    }


}
