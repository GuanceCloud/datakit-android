package com.ft.sdk;

import androidx.annotation.NonNull;

/**
 * 生产环境类型
 */
public enum EnvType {
    PROD,//线上环境
    GRAY,//灰度环境
    PRE,//预发环境
    COMMON,//日常环境
    LOCAL;//本地环境

    @NonNull
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}

