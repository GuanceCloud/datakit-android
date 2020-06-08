package com.ft.sdk.garble.bean;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
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
        try{
            js.put("__name",name);
            if(tags == null){
                tags = new HashMap<>();
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
