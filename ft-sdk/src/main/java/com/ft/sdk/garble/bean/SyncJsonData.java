package com.ft.sdk.garble.bean;


import androidx.annotation.NonNull;

import com.ft.sdk.SyncDataHelper;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;
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

    /**
     * 同步数据唯一 id, 同步上传过程中才会赋值
     */
    long id;

    /**
     * 同步数据类型
     */
    DataType dataType;

    /**
     * 同步数据字符类型数据，行协议数据
     */
    String dataString;

    /**
     * 同步数据 json 数据。同步上传过程中不会设置这个变量
     */
    JSONObject dataJson;


    String uuid;


    public SyncJsonData(DataType dataType) {
        this.dataType = dataType;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
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
     * 标记包序列发送 id,替换 [uuid] 为 [packageId].[dataCount].[uuid]
     *
     * @param packageId 包 id
     * @param dataCount 数量
     * @return
     */
    public String getDataStringWithPackageId(String packageId, int dataCount) {
        if (packageId != null) {
            dataString = dataString.replace(uuid, packageId + "." + dataCount + "." + uuid);
        }
        return dataString;
    }

    /**
     * 替换 (sdk_data_id=)([0-9a-z]+).([0-9]+).[uuid] 为 sdk_data_id=[uuid]
     * @param newUUID 新 uuid
     * @return
     */
    public String getDataString(String newUUID) {
        if (newUUID != null) {
            dataString = dataString.replaceFirst("(" + Constants.KEY_SDK_DATA_FLAG + "=)([0-9a-z]+).([0-9]+)."
                    + uuid, Constants.KEY_SDK_DATA_FLAG + "=" + newUUID);
        }
        return dataString;
    }


    public JSONObject getDataJson() {
        return dataJson;
    }

    public void setDataJson(JSONObject json) {
        this.dataJson = json;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
    public static SyncJsonData getSyncJsonData(SyncDataHelper helper, DataType dataType, LineProtocolBean bean)
            throws JSONException, FTInvalidParameterException {
        JSONObject tagsTemp = bean.getTags();
        JSONObject fields = bean.getFields();
        SyncJsonData recordData = new SyncJsonData(dataType);
        recordData.setTime(bean.getTimeNano());
        recordData.setUuid(Utils.randomUUID());
        recordData.setDataJson(getLinProtocolJson(bean.getMeasurement(), tagsTemp, fields));
        recordData.setDataString(helper.getBodyContent(recordData));
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
    public static SyncJsonData getFromLogBean(SyncDataHelper helper, BaseContentBean bean, DataType dataType)
            throws JSONException, FTInvalidParameterException {
        SyncJsonData recordData = new SyncJsonData(DataType.LOG);
        recordData.setTime(bean.getTime());
        recordData.setUuid(Utils.randomUUID());
        recordData.setDataJson(getLinProtocolJson(bean.getMeasurement(), bean.getAllTags(), bean.getAllFields()));
        recordData.setDataString(helper.getBodyContent(recordData));
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
