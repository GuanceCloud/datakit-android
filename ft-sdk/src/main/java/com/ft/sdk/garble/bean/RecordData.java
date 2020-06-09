package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTFlowChartConfig;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * BY huangDianHua
 * DATE:2019-12-02 13:56
 * Description:操作记录类
 */
public class RecordData implements Cloneable {

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
     * 父页面的名称
     */
    private String ppn;
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

    /**
     * 流程图唯一ID
     */
    private String traceId;

    /**
     * 操作的时间间隔
     */
    private long duration;

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

    public String getPpn() {
        return ppn;
    }

    public void setPpn(String ppn) {
        this.ppn = ppn;
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

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
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

            if (FTFlowChartConfig.get().isOpenFlowChart()) {
                if (!Utils.isNullOrEmpty(ppn)) {
                    recordData.put("ppn", ppn);
                }
                if (!Utils.isNullOrEmpty(traceId)) {
                    recordData.put("traceId", traceId);
                }
                recordData.put("duration", duration);
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
            ppn = jsonObject.optString("ppn");
            traceId = jsonObject.optString("traceId");
            duration = jsonObject.optInt("duration");

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

    @NonNull
    @Override
    public RecordData clone() throws CloneNotSupportedException {
        return (RecordData) super.clone();
    }
}
