package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

/**
 * 应用运行状态，通过查看应用标记运用状态来区分 App 的运行状态
 */
public enum AppState {
    /**
     *  未知状态，当获取不到时，标记为 Unknown
     */
    UNKNOWN,
    /**
     * 应用启动
     */
    STARTUP,
    /**
     * 运行中
     */
    RUN;

    /**
     * 从字符转化对应的
     * @param value @{@link AppState} 对应字符
     * @return 返回当前运行状态
     */
    public static AppState getValueFrom(String value) {
        AppState[] states = AppState.values();
        for (int i = 0; i < AppState.values().length; i++) {
            AppState state = states[i];
            if (state.toString().toLowerCase().equals(value)) {
                return state;
            }
        }
        return UNKNOWN;
    }

    /**
     * 用于行协议传参，行协议中均为小写
     * @return 小写字符 unknown，startup，run
     */
    @NonNull
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
