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
import com.ft.plugin.garble.Constants;
import com.ft.plugin.garble.Logger;
import com.ft.plugin.garble.VersionUtils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * 本类借鉴修改了来自 Sensors Data  的项目<a href="https://github.com/sensorsdata/sa-sdk-android-plugin2">sa-sdk-android-plugin2</a>
 * 中的 SensorsAnalyticsClassVisitor.groovy 类
 */
public class FTClassAdapter extends ClassVisitor {
    private String className;
    private String superName;
    private String[] interfaces;
    private final List<String> ignorePackages;
    /**
     * 是否跳过
     */
    private boolean needSkip;

    public FTClassAdapter(final ClassVisitor cv, int api, List<String> ignorePackages) {
        super(api, cv);
        this.ignorePackages = ignorePackages == null ? new ArrayList<>() : ignorePackages;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        this.superName = superName;
        this.interfaces = interfaces;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (Constants.IGNORE_ANNOTATION.equalsIgnoreCase(descriptor)) {
            needSkip = true;
        }
        return super.visitAnnotation(descriptor, visible);
    }

    /**
     * 访问类中定义的字段
     *
     * @param access
     * @param name
     * @param desc
     * @param signature
     * @param value
     * @return
     */
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (ClassNameAnalytics.isFTSdkApi(className)) {//判断是否是 FTSdk 类
            if (name.equals("AGENT_VERSION")) {//获得 AGENT_VERSION 判断是否高于最低版本
                String agentVersion = (String) value;
                if (!VersionUtils.firstVerGreaterEqual(agentVersion, BuildConfig.MIN_SDK_VERSION)) {
                    String errorTip = "你目前集成的 FT SDK 的版本为 " + agentVersion + ",当前插件支持 SDK 的最低版本为 " + BuildConfig.MIN_SDK_VERSION
                            + ",详细信息请参考：https://github.com/GuanceCloud/datakit-android";
                    throw new Error(errorTip);
                }
            }
        }
        return super.visitField(access, name, desc, signature, value);
    }

    /**
     * 访问类方法
     *
     * @param access
     * @param name
     * @param desc
     * @param signature
     * @param exceptions
     * @return
     */
    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (needSkip) {
            Logger.debug("ignoreAOP-> class:" + className + ",super:" + superName + ", method:" + name + desc);
            return mv;
        } else if (isIgnorePackage(className)) {
            Logger.debug("isIgnorePackage-> class:" + className + ",super:" + superName + ", method:" + name + desc);
            return mv;
        }
        return new FTMethodAdapter(mv, access, name, desc, className, interfaces, superName, api);
    }

    private boolean isIgnorePackage(String className) {
        boolean isPackageIgnore = false;
        for (String packageName : ignorePackages) {
            if (className.startsWith(packageName.replace(".", "/"))) {
                isPackageIgnore = true;
                break;
            }
        }
        return isPackageIgnore;
    }
}