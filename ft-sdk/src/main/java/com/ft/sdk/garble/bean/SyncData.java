package com.ft.sdk.garble.bean;


import androidx.annotation.NonNull;

import com.ft.sdk.SyncDataHelper;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.HashMapUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.internal.exception.FTInvalidParameterException;

import org.json.JSONException;

import java.security.InvalidParameterException;


/**
 * BY huangDianHua
 * DATE:2019-12-02 13:56
 * Description: Data storage Data JSON data
 */
public class SyncData implements Cloneable {
    /**
     * Sync data unique ID, assigned only during sync upload process
     */
    long id;

    /**
     * Sync data type
     */
    DataType dataType;

    /**
     * Sync data character type data, old data is JSON, new data is line protocol
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
     * Operation time
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
     * Mark package sequence send ID, replace [uuid] with [packageId].[uuid]
     *
     * @param packageId Package ID
     * @return
     */
    public String getLineProtocolDataWithPkgId(String packageId) {
        if (packageId != null) {
            dataString = dataString.replaceFirst(uuid, packageId + "." + uuid);
        }
        return dataString;
    }

    /**
     * Replace (sdk_data_id=)[packageId].[pid].[pkg_dataCount].[uuid] with sdk_data_id=[uuid]
     *
     * @param newUUID New UUID
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
     * Trace data conversion
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
        if (dataGenerateTime > 0) {
            recordData.setTime(dataGenerateTime);
        } else {
            recordData.setTime(bean.getTimeNano());
        }
        if (bean.getMeasurement().equals(Constants.FT_MEASUREMENT_RUM_VIEW)) {
            String uuidFromView = HashMapUtils.getString(bean.getTags(), Constants.KEY_RUM_VIEW_ID);
            uuid = uuidFromView == null ? uuid : uuidFromView.substring(0, 16);
            recordData.setUuid(uuid);
        } else {
            recordData.setUuid(uuid);
        }
        recordData.setDataString(helper.getBodyContent(bean.getMeasurement(), bean.getTags(),
                bean.getFields(), bean.getTimeNano(), dataType, uuid));
        return recordData;
    }


    /**
     * Log data conversion
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
