package com.ft.sdk.garble.bean;

import android.content.Context;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * create: by huangDianHua
 * time: 2020/6/8 17:43:20
 * description:
 */
public class ObjectBean {
    private String name;
    private HashMap<String,Object> tags;
    private String clazz;

    public ObjectBean(String name,String clazz){
        this.name = name;
        this.clazz = clazz;
    }

    public ObjectBean(String name,String clazz,HashMap<String,Object> tags){
        this.name = name;
        this.clazz = clazz;
        this.tags = tags;
    }

    public JSONObject getJSONData(){
        JSONObject js = new JSONObject();
        Context context = FTApplication.getApplication();
        try{
            if(Utils.isNullOrEmpty(name)){
                name = DeviceUtils.getUuid(context);
            }
            js.put("__name",name);
            if(tags == null){
                tags = new HashMap<>();
            }
            HashMap<String,Object> hashMap = getDefaultObjectBean();
            Iterator<Map.Entry<String,Object>> iteratorDefault= hashMap.entrySet().iterator();
            while (iteratorDefault.hasNext()) {
                Map.Entry<String,Object> entry = iteratorDefault.next();
                if(!tags.containsKey(entry.getKey())) {
                    tags.put(entry.getKey(), entry.getValue());
                }
            }
            if(Utils.isNullOrEmpty(clazz)){
                clazz = Constants.DEFAULT_OBJECT_CLASS;
            }
            tags.put("__class",clazz);
            JSONObject jsonObject = new JSONObject();
            Iterator<Map.Entry<String,Object>> iterator = tags.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String,Object> entry = iterator.next();
                jsonObject.put(entry.getKey(),entry.getValue());
            }
            js.put("__tags",jsonObject);
        }catch (Exception e){
            e.printStackTrace();
        }
        return js;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Object> getTags() {
        return tags;
    }

    public void setTags(HashMap<String, Object> tags) {
        this.tags = tags;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    /**
     * 构建一个默认的 ObjectBean
     * @return
     */
    private HashMap<String, Object> getDefaultObjectBean(){
        Context context = FTApplication.getApplication();
        HashMap<String, Object> objectHashMap = new HashMap<>();
        objectHashMap.put("device_uuid", DeviceUtils.getUuid(context));
        objectHashMap.put("application_identifier", DeviceUtils.getApplicationId(context));
        objectHashMap.put("application_name", DeviceUtils.getAppName(context));
        objectHashMap.put("agent", DeviceUtils.getSDKVersion());
        objectHashMap.put("autoTrack", FTSdk.PLUGIN_VERSION);
        objectHashMap.put("os", DeviceUtils.getOSName());
        objectHashMap.put("os_version", DeviceUtils.getOSVersion());
        objectHashMap.put("device_band", DeviceUtils.getDeviceBand());
        objectHashMap.put("device_model", DeviceUtils.getDeviceModel());
        objectHashMap.put("display", DeviceUtils.getDisplay(context));
        objectHashMap.put("carrier", DeviceUtils.getCarrier(context));
        objectHashMap.put("locale", Locale.getDefault());
        return objectHashMap;
    }
}
