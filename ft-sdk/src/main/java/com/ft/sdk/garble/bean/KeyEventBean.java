package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * create: by huangDianHua
 * time: 2020/6/8 16:10:17
 * description:
 */
public class KeyEventBean {
    private String measurement = "__keyevent";
    private String eventId;
    private String source;
    private String status;
    private String ruleId;
    private String ruleName;
    private String type;
    private String actionType;
    private String title;
    private String content;
    private String suggestion;
    private long duration;
    private JSONArray dimensions;
    private JSONObject tags;
    private JSONObject fields;
    private long time;

    public KeyEventBean(String title,long time){
        this.title = title;
        this.time = time;
    }

    public JSONObject getAllTags(){
        if(tags == null){
            tags = new JSONObject();
        }
        try {
            if (!Utils.isNullOrEmpty(eventId)) {
                tags.put("__eventId", eventId);
            }
            if(!Utils.isNullOrEmpty(source)){
                tags.put("__source",source);
            }
            if(!Utils.isNullOrEmpty(status)){
                tags.put("__status",status);
            }
            if(!Utils.isNullOrEmpty(ruleId)){
                tags.put("__ruleName",ruleName);
            }
            if(!Utils.isNullOrEmpty(type)){
                tags.put("__type",type);
            }
            if(!Utils.isNullOrEmpty(actionType)){
                tags.put("__actionType",actionType);
            }
        }catch (Exception e){

        }
        return tags;
    }

    public JSONObject getAllFields(){
        if(fields == null){
            fields = new JSONObject();
        }
        try{
            if(!Utils.isNullOrEmpty(title)){
                fields.put("__title",title);
            }
            if(!Utils.isNullOrEmpty(content)){
                fields.put("__content",content);
            }
            if(!Utils.isNullOrEmpty(suggestion)){
                fields.put("_suggestion",suggestion);
            }
            fields.put("__duration",duration);
            fields.put("__dimensions",dimensions);
        }catch (Exception e){

        }
        return fields;
    }
    public String getMeasurement() {
        return measurement;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public JSONArray getDimensions() {
        return dimensions;
    }

    public void setDimensions(JSONArray dimensions) {
        this.dimensions = dimensions;
    }

    public JSONObject getTags() {
        return tags;
    }

    public void setTags(JSONObject tags) {
        this.tags = tags;
    }

    public JSONObject getFields() {
        return fields;
    }

    public void setFields(JSONObject fields) {
        this.fields = fields;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
