package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.FloatDoubleJsonUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import static com.ft.sdk.garble.manager.SyncDataHelper.addMonitorData;

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


    /**
     * 用户数据关联ID
     */
    private String sessionid;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }


    public DataType getDataType() {
        return dataType;
    }


    public String getSessionId() {
        return sessionid;
    }

    public void setSessionId(String sessionid) {
        this.sessionid = sessionid;
    }


    @Override
    public String toString() {
        return "SyncJsonData{" +
                "id=" + id +
                ", dataType=" + dataType +
                ", dataString='" + dataString + '\'' +
                ", time=" + time +
                ", sessionid='" + sessionid + '\'' +
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
     * 获得格式化的打印内容
     *
     * @return
     */
    public String printFormatRecordData() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("\n{");
        buffer.append("\n\t\"sessionId\":\"" + sessionid + "\",");
        buffer.append("\n\t\"opData\":\"" + dataString + "\"");
        buffer.append("\n}");
        return buffer.toString();
    }

    public static SyncJsonData getFromObjectData(ObjectBean objectBean) {
        ArrayList<ObjectBean> list = new ArrayList<>();
        list.add(objectBean);
        return getFromObjectList(list);
    }

    public static SyncJsonData getFromObjectList(List<ObjectBean> datas) {

        SyncJsonData jsonData = new SyncJsonData(DataType.OBJECT);
        JSONArray array = new JSONArray();
        for (ObjectBean data : datas
        ) {
            array.put(data.getJSONData());

        }
        jsonData.setDataString(array.toString());

        return jsonData;

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
            throws JSONException, InvalidParameterException {
        JSONObject tagsTemp = bean.getTags();
        JSONObject fields = bean.getFields();
        SyncJsonData recordData = new SyncJsonData(dataType);
        recordData.setTime(bean.getTimeNano());
        JSONObject opDataJson = getLinProtocolJson(bean.getMeasurement(), tagsTemp, fields);

        recordData.setDataString(FloatDoubleJsonUtils.protectValueFormat(opDataJson));

        String sessionId = FTUserConfig.get().getSessionId();
        if (!Utils.isNullOrEmpty(sessionId)) {
            recordData.setSessionId(sessionId);
        }

        return recordData;
    }

    public static SyncJsonData getMonitorData() throws JSONException {
        JSONObject tags = new JSONObject();
        JSONObject fields = new JSONObject();
        addMonitorData(tags, fields);
        SyncJsonData recordData = new SyncJsonData(DataType.TRACK);

        JSONObject opDataJson = getLinProtocolJson(Constants.FT_MONITOR_MEASUREMENT, tags, fields);
        recordData.setDataString(FloatDoubleJsonUtils.protectValueFormat(opDataJson));
        recordData.setTime(Utils.getCurrentNanoTime());
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
    public static SyncJsonData getFromLogBean(BaseContentBean bean,DataType dataType)
            throws JSONException, InvalidParameterException {
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
            throws JSONException, InvalidParameterException {

        JSONObject tagsTemp = tags;

        JSONObject opDataJson = new JSONObject();


        if (measurement != null) {
            opDataJson.put(Constants.MEASUREMENT, measurement);
        } else {
            throw new InvalidParameterException("指标集 measurement 不能为空");
        }
        if (tagsTemp == null) {
            tagsTemp = new JSONObject();
        }
        opDataJson.put(Constants.TAGS, tagsTemp);
        if (fields != null) {
            opDataJson.put(Constants.FIELDS, fields);
        } else {
            throw new InvalidParameterException("指标集 fields 不能为空");
        }
        return opDataJson;

    }


}
