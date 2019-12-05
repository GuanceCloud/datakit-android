package com.ft.sdk.garble.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * BY huangDianHua
 * DATE:2019-12-02 13:56
 * Description:操作记录类
 */
public class RecordData {
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
    private OP op;
    /**
     * 操作数据
     */
    private OpData opdata;

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

    public OP getOp() {
        return op;
    }

    public void setOp(OP op) {
        this.op = op;
    }

    public OpData getOpdata() {
        return opdata;
    }

    public void setOpdata(OpData opdata) {
        this.opdata = opdata;
    }

    public String getJsonString() {
        JSONObject recordData = new JSONObject();
        try {
            if(cpn != null) {
                recordData.put("cnp", cpn);
            }

            if(rpn != null) {
                recordData.put("rpn", rpn);
            }

            if(op.value != null) {
                recordData.put("op", op.value);
            }

            if (opdata != null && !opdata.isEmpty()) {
                recordData.put("opdata", opdata.getJson());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return recordData.toString();
    }

    /**
     * 操作的数据类
     */
    public class OpData {
        /**
         * 视图树
         */
        private String vtp;
        private String field;

        public String getVtp() {
            return vtp;
        }

        public void setVtp(String vtp) {
            this.vtp = vtp;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public boolean isEmpty(){
            return (vtp == null || vtp.isEmpty()) && (field == null || field.isEmpty());
        }

        public JSONObject getJson() throws JSONException {
            JSONObject opdata = new JSONObject();
            opdata.put("vtp", vtp);
            return opdata;
        }
    }
}
