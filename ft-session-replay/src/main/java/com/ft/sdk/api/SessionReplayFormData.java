package com.ft.sdk.api;

import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class SessionReplayFormData {
    // Get normal form fields
    Map<String, String> fields;

    // Get files to upload
    HashMap<String, Pair<String,byte[]>> fileFields;


    public SessionReplayFormData(Map<String, String> fields, HashMap<String, Pair<String,byte[]>> fileFields) {
        this.fields = fields;
        this.fileFields = fileFields;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public HashMap<String,  Pair<String,byte[]>> getFileFields() {
        return fileFields;
    }

    public void setFileFields(HashMap<String,  Pair<String,byte[]>> fileFields) {
        this.fileFields = fileFields;
    }
}