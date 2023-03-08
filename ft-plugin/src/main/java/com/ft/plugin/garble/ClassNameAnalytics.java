/*
 * Created by wangzhuohou on 2015/08/01.
 * Copyright 2015－2020 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ft.plugin.garble;

/**
 * 本类借鉴修改了来自 Sensors Data 的项目 <a href="https://github.com/sensorsdata/sa-sdk-android-plugin2">sa-sdk-android-plugin2</a>
 * 中的 ClassNameAnalytics 类
 *
 * @author huangdianhua
 */
public class ClassNameAnalytics {
    public static final String FT_SDK_PACKAGE = "com.ft.sdk";
    public static final String FT_SDK = "com.ft.sdk.FTSdk";

    /**
     * 配置需要排除的类
     *
     * @param className
     * @return
     */
    public static boolean isFTSDKFile(String className) {
        return className.startsWith(FT_SDK_PACKAGE) && !className.equals(FT_SDK + ".class");
    }

    public static boolean isFTSdkApi(String className) {
        return className.equals(FT_SDK);
    }

    public static boolean isDCloud(String className) {
        return className.contains(Constants.CLASS_NAME_DCLOUD_PATH);
    }

    public static boolean isTencent(String className) {
        return className.contains(Constants.CLASS_NAME_TENCENT_PATH);
    }

    public static boolean isTaoBao(String className) {
        return className.contains(Constants.CLASS_NAME_TAOBAO_PATH);
    }

    public static boolean isAndroidGenerated(String className) {
        return className.contains("R$") ||
                className.contains("R2$") ||
                className.contains("R.class") ||
                className.contains("R2.class") ||
                className.contains("BuildConfig.class");
    }
}
