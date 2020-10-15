package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.utils.Constants;
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

    /**
     * 操作数据
     */
    OPData opData;

    public SyncJsonData(DataType dataType) {
        this.dataType = dataType;
    }


    public OPData getOpData() {
        return opData;
    }

    public void setDataString(String dataString) {
        try {
            if (dataType == DataType.TRACK) {
                JSONObject jsonObject = new JSONObject(dataString);
                opData = new OPData();
                opData.setOpFromString(jsonObject.optString("op"));
                opData.setContent(jsonObject.optString("opdata"));
            }
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


    @NonNull
    @Override
    public String toString() {
        return "RecordData[id=" + id +
                ",time=" + time +
                ",data=" + dataString +
                "]";
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
     * @param bean
     * @param op
     * @return
     * @throws JSONException
     * @throws InvalidParameterException
     */
    public static SyncJsonData getFromTrackBean(TrackBean bean, OP op) throws JSONException, InvalidParameterException {
        JSONObject tagsTemp = bean.getTags();

        JSONObject fields = bean.getFields();
        SyncJsonData recordData = new SyncJsonData(DataType.TRACK);
        recordData.setTime(bean.getTimeMillis());


        if (op.needMonitorData()) {
            addMonitorData(tagsTemp, fields);
        }

        JSONObject opDataJson = getLinProtocolJson(bean.getMeasurement(), tagsTemp, fields);

        OPData opData = new OPData();
        opData.setOp(op);
        opData.setContent(opDataJson.toString());
        recordData.setDataString(opData.toJsonString());

        String sessionId = FTUserConfig.get().getSessionId();
        if (!Utils.isNullOrEmpty(sessionId)) {
            recordData.setSessionId(sessionId);
        }

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
    public static SyncJsonData getFromLogBean(LogBean bean)
            throws JSONException, InvalidParameterException {
        SyncJsonData recordData = new SyncJsonData(DataType.LOG);
        recordData.setTime(bean.getTime());
        JSONObject opDataJson = getLinProtocolJson(bean.getMeasurement(), bean.getAllTags(), bean.getAllFields());
        recordData.setDataString(opDataJson.toString());

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
