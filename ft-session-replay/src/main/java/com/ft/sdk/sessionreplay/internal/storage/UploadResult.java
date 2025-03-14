package com.ft.sdk.sessionreplay.internal.storage;

import java.net.HttpURLConnection;

public class UploadResult {

    private boolean needReTry = false;
    private boolean success = false;
    private final String response;
    private final int code;
    private final String pkgId;

    public UploadResult(int code, String response, String pkgId) {
        if (code == HttpURLConnection.HTTP_OK) {
            success = true;

        } else if (code >= HttpURLConnection.HTTP_BAD_REQUEST
                && code < HttpURLConnection.HTTP_INTERNAL_ERROR) {

        } else {
            needReTry = true;
        }
        this.code = code;
        this.response = response;
        this.pkgId = pkgId;
    }

    public boolean isNeedReTry() {
        return needReTry;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getResponse() {
        return response;
    }

    public int getCode() {
        return code;
    }

    public String getPkgId() {
        return pkgId;
    }
}
