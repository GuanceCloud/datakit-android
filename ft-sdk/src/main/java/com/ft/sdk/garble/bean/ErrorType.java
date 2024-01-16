package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

/**
 * @author Brandon
 */
public enum ErrorType {
    /**
     * C++/C 类型崩溃
     */
    NATIVE("native_crash"),
    /**
     * Java 类型崩溃
     */
    JAVA("java_crash"),
    /**
     * {<a href="https://github.com/GuanceCloud/datakit-flutter">Flutter SDK </a>}
     */
    FLUTTER("flutter_crash"),

    /**
     * 由于 ANR 产生的崩溃问题
     */
    ANR_CRASH("anr_crash"),

    /**
     * 触发的 ANR 事件
     */
    ANR_ERROR("anr_error"),
    /**
     * 网络请求错误
     */
    NETWORK("network_error");


    /**
     * 错误类型，最终显示在行协议中的字符内容
     */
    private final String value;

    ErrorType(String value) {
        this.value = value;
    }


    /**
     * 从字符转化成对应 ErrorType
     *
     * @param value native_crash, java_crash, flutter_crash, network_error
     * @return {@link ErrorType} 类型
     */
    public static ErrorType getValueFrom(String value) {
        ErrorType[] errorTypes = values();

        for (int i = 0; i < values().length; ++i) {
            ErrorType state = errorTypes[i];
            if (state.toString().toLowerCase().equals(value)) {
                return state;
            }
        }

        return NATIVE;
    }


    @NonNull
    @Override
    public String toString() {
        return value;
    }
}
