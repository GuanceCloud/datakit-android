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
package com.ft.plugin.garble.bytecode;

import com.ft.plugin.BuildConfig;
import com.ft.plugin.garble.ClassNameAnalytics;
import com.ft.plugin.garble.FTTransformHelper;
import com.ft.plugin.garble.VersionUtils;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import static com.ft.plugin.garble.FTUtil.ASM_VERSION;

/**
 * 本类借鉴修改了来自 Sensors Data 的项目 https://github.com/sensorsdata/sa-sdk-android-plugin2
 * 中的 SensorsAnalyticsClassVisitor.groovy 类
 */
public class FTClassAdapter extends ClassVisitor {
    private String className;
    private String superName;
    private String[] interfaces;
    private FTTransformHelper ftTransformHelper;
    private boolean needChangeField;

    FTClassAdapter(final ClassVisitor cv, FTTransformHelper ftTransformHelper) {
        super(ASM_VERSION, cv);
        this.ftTransformHelper = ftTransformHelper;
        //Logger.info(">>>> goon scan class ");
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        this.superName = superName;
        this.interfaces = interfaces;
        //Logger.info(">>>> start scan class ----> " + className + ", superName=" + superName);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (ClassNameAnalytics.isFTSdkApi(className.replaceAll("/", "."))) {
            if (name.equals("AGENT_VERSION")) {
                String agentVersion = (String) value;
                if (!VersionUtils.firstVerGreaterEqual(agentVersion, BuildConfig.MIN_SDK_VERSION)) {
                    String errorTip = "你目前集成的 FT SDK 的版本为 " + agentVersion + ",当前插件支持 SDK 的最低版本为 " + BuildConfig.MIN_SDK_VERSION
                            + ",详细信息请参考：https://github.com/CloudCare/dataflux-sdk-android";
                    throw new Error(errorTip);
                }
            }
            if (name.equals("PLUGIN_MIN_VERSION")) {
                String pluginMinVersion = (String) value;
                if (!VersionUtils.firstVerGreaterEqual(BuildConfig.PLUGIN_VERSION, pluginMinVersion)) {
                    String errorTip = "你目前集成的 FT Plugin 的版本为 " + BuildConfig.PLUGIN_VERSION + ",当前 SDK 支持的插件的最低版本为 " + pluginMinVersion
                            + ",详细信息请参考：https://github.com/CloudCare/dataflux-sdk-android";
                    throw new Error(errorTip);
                }
            }
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        FTMethodAdapter ftMethodAdapter = new FTMethodAdapter(mv, access, name, desc, className, interfaces, superName, ftTransformHelper);
        return ftMethodAdapter;
    }
}