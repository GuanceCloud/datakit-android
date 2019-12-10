package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

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
    private OpData opdata;

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

    public OpData getOpdata() {
        return opdata;
    }

    public void setOpdata(OpData opdata) {
        this.opdata = opdata;
    }


    public String getJsonString() {
        JSONObject recordData = new JSONObject();
        try {
            if(!Utils.isNullOrEmpty(cpn)) {
                recordData.put("cpn", cpn);
            }

            if(!Utils.isNullOrEmpty(rpn)) {
                recordData.put("rpn", rpn);
            }

            if(!Utils.isNullOrEmpty(op)) {
                recordData.put("op", op);
            }

            if (opdata != null && !opdata.isEmpty()) {
                recordData.put("opdata", opdata.getJson());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return recordData.toString();
    }

    public void parseJsonToObj(String json){
        try{
            JSONObject jsonObject = new JSONObject(json);
            cpn = jsonObject.optString("cpn");
            rpn = jsonObject.optString("rpn");
            op = jsonObject.optString("op");
            JSONObject opData = jsonObject.optJSONObject("opdata");
            if(opData != null) {
                OpData opData1 = new OpData();
                opData1.vtp = opData.optString("vtp");
                opData1.field = opData.optString("field");
                opdata = opData1;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String composeUpdateData(){
        StringBuffer sb = new StringBuffer();
        if(!Utils.isNullOrEmpty(cpn)){
            sb.append("current_page_name="+cpn+",");
        }
        if(!Utils.isNullOrEmpty(rpn)){
            sb.append("root_page_name="+rpn+",");
        }
        if(opdata != null){
            if(!Utils.isNullOrEmpty(opdata.vtp)){
                sb.append("vtp="+opdata.vtp+",");
            }
            if(!Utils.isNullOrEmpty(opdata.field)){
                sb.append("field="+opdata.field+",");
            }
        }
        int index = sb.lastIndexOf(",");
        if (index > 0 && index == sb.length()-1) {
            sb.deleteCharAt(sb.length()-1);
        }
        if(sb.length()>0){
            sb.insert(0,",");
        }
        sb.append(" ");
        sb.append("event=\""+op+"\"");
        sb.append(" ");
        sb.append(time*1000000);
        return sb.toString();
    }

    @NonNull
    @Override
    public String toString() {
        return "RecordData[id="+id+
                ",time="+time+
                ",data="+getJsonString()+
                "]";
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
            if(!Utils.isNullOrEmpty(vtp)) {
                opdata.put("vtp", vtp);
            }
            if(!Utils.isNullOrEmpty(field)) {
                opdata.put("field", field);
            }
            return opdata;
        }
    }
}
