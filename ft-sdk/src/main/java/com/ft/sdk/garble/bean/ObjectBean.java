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
    private String name;//当前对象的名称，同一个分类下，对象名称如果重复，会覆盖原有数据
    private HashMap<String,Object> tags;//当前对象的标签，key-value 对，其中存在保留标签
    private String clazz;//当前对象的分类，分类值用户可自定义

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
}
