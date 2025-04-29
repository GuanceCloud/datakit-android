package com.ft.sdk.garble.bean;


import androidx.annotation.NonNull;

import com.ft.sdk.SyncDataHelper;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.internal.exception.FTInvalidParameterException;

import org.json.JSONException;

import java.security.InvalidParameterException;


/**
 * BY huangDianHua
 * DATE:2019-12-02 13:56
 * Description: 数据存储 Data Json 数据
 */
public class SyncData implements Cloneable {
    /**
     * 同步数据唯一 id, 同步上传过程中才会赋值
     */
    long id;

    /**
     * 同步数据类型
     */
    DataType dataType;

    /**
     * 同步数据字符类型数据，旧数据数据为 json，新数据为行协议
     */
    String dataString;


    String uuid;


    public SyncData(DataType dataType) {
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
        return "SyncData{" +
                "id=" + id +
                ", dataType=" + dataType +
                ", dataString='" + dataString + '\'' +
                ", time=" + time +
                '}';
    }

    @NonNull
    @Override
    public SyncData clone() throws CloneNotSupportedException {
        return (SyncData) super.clone();
    }

    public String getDataString() {
        return dataString;
    }

    /**
     * 标记包序列发送 id,替换 [uuid] 为 [packageId].[uuid]
     *
     * @param packageId 包 id
     * @return
     */
    public String getLineProtocolDataWithPkgId(String packageId) {
        if (packageId != null) {
            dataString = dataString.replaceFirst(uuid, packageId + "." + uuid);
        }
        return dataString;
    }

    /**
     * 替换 (sdk_data_id=)[packageId].[pid].[pkg_dataCount].[uuid] 为 sdk_data_id=[uuid]
     *
     * @param newUUID 新 uuid
     * @return
     */
    public String getDataString(String newUUID) {
        if (newUUID != null) {
            dataString = dataString.replaceFirst("(" +
                    Constants.KEY_SDK_DATA_FLAG + "=)(.*)"
                    + uuid, Constants.KEY_SDK_DATA_FLAG + "=" + newUUID);
        }
        return dataString;
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
     * @throws InvalidParameterException
     */
    public static SyncData getSyncData(SyncDataHelper helper, DataType dataType, LineProtocolBean bean,
                                       long dataGenerateTime)
            throws FTInvalidParameterException {
        String uuid = Utils.getGUID_16();
        SyncData recordData = new SyncData(dataType);
        if (bean.getMeasurement().equals(Constants.FT_MEASUREMENT_RUM_VIEW)) {
            recordData.setTime(dataGenerateTime);
        } else {
            recordData.setTime(bean.getTimeNano());
        }
        recordData.setUuid(uuid);
        recordData.setDataString(helper.getBodyContent(bean.getMeasurement(), bean.getTags(),
                bean.getFields(), bean.getTimeNano(), dataType, uuid));
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
    public static SyncData getFromLogBean(SyncDataHelper helper, BaseContentBean bean)
            throws FTInvalidParameterException {
        SyncData recordData = new SyncData(DataType.LOG);
        String uuid = Utils.getGUID_16();
        recordData.setTime(bean.getTimeNano());
        recordData.setUuid(uuid);
        recordData.setDataString(helper.getBodyContent(bean.getMeasurement(),
                bean.getAllTags(), bean.getAllFields(), bean.getTimeNano(), DataType.LOG, uuid));
        return recordData;
    }

}
