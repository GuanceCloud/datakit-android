package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import static com.ft.sdk.garble.bean.OP.CSTM;
import static com.ft.sdk.garble.utils.Constants.FT_DEFAULT_MEASUREMENT;
import static com.ft.sdk.garble.utils.Constants.FT_KEY_VALUE_NULL;

/**
 * BY huangDianHua
 * DATE:2019-12-02 13:56
 * Description:操作记录类
 */
public class RecordData {
    private long id;
    /**
     * 操作时间
     */
    private long time;
    /**
     * 当前页面名称
     */
    private String cpn;
    /**
     * 根部页面名称
     */
    private String rpn;
    /**
     * 操作类型
     */
    private String op;
    /**
     * 操作数据
     */
    private String opdata;

    /**
     * 用户数据关联ID
     */
    private String sessionid;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getCpn() {
        return cpn;
    }

    public void setCpn(String cpn) {
        this.cpn = cpn;
    }

    public String getRpn() {
        return rpn;
    }

    public void setRpn(String rpn) {
        this.rpn = rpn;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getOpdata() {
        return opdata;
    }

    public void setOpdata(String opdata) {
        this.opdata = opdata;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getJsonString() {
        JSONObject recordData = new JSONObject();
        try {
            if (!Utils.isNullOrEmpty(cpn)) {
                recordData.put("cpn", cpn);
            }

            if (!Utils.isNullOrEmpty(rpn)) {
                recordData.put("rpn", rpn);
            }

            if (!Utils.isNullOrEmpty(op)) {
                recordData.put("op", op);
            }

            if (!Utils.isNullOrEmpty(opdata)) {
                recordData.put("opdata", opdata);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return recordData.toString();
    }

    public void parseJsonToObj(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            cpn = jsonObject.optString("cpn");
            rpn = jsonObject.optString("rpn");
            op = jsonObject.optString("op");
            opdata = jsonObject.optString("opdata");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "RecordData[id=" + id +
                ",time=" + time +
                ",data=" + getJsonString() +
                "]";
    }
}
