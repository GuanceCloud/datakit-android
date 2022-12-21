package com.ft.sdk.garble.bean;


import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.FloatDoubleJsonUtils;
import com.ft.sdk.internal.exception.FTInvalidParameterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;


/**
 * BY huangDianHua
 * DATE:2019-12-02 13:56
 * Description: 数据存储 Data Json 数据
 */
public class SyncJsonData implements Cloneable {

    private static final String TAG = "SyncJsonData";

    long id;
    DataType dataType;

    String dataString;

    public SyncJsonData(DataType dataType) {
        this.dataType = dataType;
    }

    public void setDataString(String dataString) {
        try {
            this.dataString = dataString;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * 操作时间
     */
    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }


    public DataType getDataType() {
        return dataType;
    }


    @Override
    public String toString() {
        return "SyncJsonData{" +
                "id=" + id +
                ", dataType=" + dataType +
                ", dataString='" + dataString + '\'' +
                ", time=" + time +
                '}';
    }

    @NonNull
    @Override
    public SyncJsonData clone() throws CloneNotSupportedException {
        return (SyncJsonData) super.clone();
    }

    public String getDataString() {
        return dataString;
    }


    /**
     * 追踪数据转化
     *
     * @param dataType
     * @param bean
     * @return
     * @throws JSONException
     * @throws InvalidParameterException
     */
    public static SyncJsonData getSyncJsonData(DataType dataType, LineProtocolBean bean)
            throws JSONException, FTInvalidParameterException {
        JSONObject tagsTemp = bean.getTags();
        JSONObject fields = bean.getFields();
        SyncJsonData recordData = new SyncJsonData(dataType);
        recordData.setTime(bean.getTimeNano());
        JSONObject opDataJson = getLinProtocolJson(bean.getMeasurement(), tagsTemp, fields);

        recordData.setDataString(FloatDoubleJsonUtils.protectValueFormat(opDataJson));
        return recordData;
    }


    /**
     * 日志数据转化
     *
     * @param bean
     * @return
     * @throws JSONException
     * @throws InvalidParameterException
     */
    public static SyncJsonData getFromLogBean(BaseContentBean bean, DataType dataType)
            throws JSONException, FTInvalidParameterException {
        SyncJsonData recordData = new SyncJsonData(dataType);
        recordData.setTime(bean.getTime());
        JSONObject opDataJson = getLinProtocolJson(bean.getMeasurement(), bean.getAllTags(), bean.getAllFields());
        recordData.setDataString(FloatDoubleJsonUtils.protectValueFormat(opDataJson));
        return recordData;
    }

    /**
     * 获取行协议对应的 指标，标签，数值对应的 Json 对象
     *
     * @param measurement
     * @param tags
     * @param fields
     * @return
     * @throws JSONException
     * @throws InvalidParameterException
     */
    private static JSONObject getLinProtocolJson(String measurement,
                                                 JSONObject tags, JSONObject fields)
            throws JSONException, FTInvalidParameterException {

        JSONObject tagsTemp = tags;

        JSONObject opDataJson = new JSONObject();


        if (measurement != null) {
            opDataJson.put(Constants.MEASUREMENT, measurement);
        } else {
            throw new FTInvalidParameterException("指标集 measurement 不能为空");
        }
        if (tagsTemp == null) {
            tagsTemp = new JSONObject();
        }
        opDataJson.put(Constants.TAGS, tagsTemp);
        if (fields != null) {
            opDataJson.put(Constants.FIELDS, fields);
        } else {
            throw new FTInvalidParameterException("指标集 fields 不能为空");
        }
        return opDataJson;

    }


}
