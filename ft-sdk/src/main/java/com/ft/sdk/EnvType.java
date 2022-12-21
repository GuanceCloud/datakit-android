package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;

/**
 * 数据环境，一般用于区分隔离数据不同生产线的数据，数据影响 <a href="https://docs.guance.com/logs/explorer/">日志查看器</a> 与
 * 用户访问监测中 <a href="https://docs.guance.com/real-user-monitoring/android/app-analysis/">Android 应用分析</a>
 * 过滤使用字段为{@link Constants#KEY_ENV }（环境），
 * <p>
 */
public enum EnvType {
    /**
     * 线上环境，默认环境
     */
    PROD,
    /**
     * 灰度环境
     */
    GRAY,
    /**
     * 预发环境
     */
    PRE,
    /**
     * 日常环境
     */
    COMMON,
    /**
     * 本地环境
     */
    LOCAL;


    /**
     * 用于行协议传参使用，行协议中均为小写
     *
     * @return 返回 prod, gray, pre, common, local
     */
    @NonNull
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

}

