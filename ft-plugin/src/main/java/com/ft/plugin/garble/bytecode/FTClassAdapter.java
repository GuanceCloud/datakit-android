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

import static com.ft.plugin.garble.bytecode.FTMethodAdapter.TARGET_WEBVIEW_METHOD;

import com.ft.plugin.BuildConfig;
import com.ft.plugin.garble.ClassNameAnalytics;
import com.ft.plugin.garble.Constants;
import com.ft.plugin.garble.FTUtil;
import com.ft.plugin.garble.Logger;
import com.ft.plugin.garble.VersionUtils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is adapted and modified from the Sensors Data project
 * <a href="https://github.com/sensorsdata/sa-sdk-android-plugin2">sa-sdk-android-plugin2</a>
 * SensorsAnalyticsClassVisitor.groovy class
 */
public class FTClassAdapter extends ClassVisitor {
    private String className;
    private String superName;
    private String[] interfaces;
    private final List<String> ignorePackages;
    /**
     * Whether to skip
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
     * Visit fields defined in the class
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
        if (ClassNameAnalytics.isFTSdkApi(className)) { // Determine if it is the FTSdk class
            if (name.equals("AGENT_VERSION")) { // Get AGENT_VERSION and determine if it is higher than the minimum version
                String agentVersion = (String) value;
                if (!VersionUtils.firstVerGreaterEqual(agentVersion, BuildConfig.MIN_SDK_VERSION)) {
                    String errorTip = "The version of FT SDK you have integrated is " + agentVersion + ", the minimum version supported by the current plugin is " + BuildConfig.MIN_SDK_VERSION
                            + ", for details please refer to: https://github.com/GuanceCloud/datakit-android";
                    throw new Error(errorTip);
                }
            }
        }
        return super.visitField(access, name, desc, signature, value);
    }

    /**
     * Visit class methods
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
        } else {
            String nameDesc = name + desc;
            boolean isSDKInner = innerSDKSkip(className);
            boolean isOkhttp = ClassNameAnalytics.isOkhttp3Path(className);
            if (FTUtil.isTargetClassInSpecial(className)
                    || isSDKInner
                    || isOkhttp || isWebViewInner(className, superName, nameDesc)) {
                if (!isSDKInner && !isOkhttp) {
                    Logger.debug("skip-> class:" + className + ",super:" + superName + ",desc:" + nameDesc);
                }
                return mv;
            }
        }
        return new FTMethodAdapter(mv, access, name, desc, className, interfaces, superName, api);
    }

    /**
     * ignorePackages corresponds to the packages or class names to be ignored
     *
     * @param className
     * @return
     */
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

    /**
     * SDK internal methods, except for {@link Constants#FT_SDK_PACKAGE}, do not need to be scanned
     *
     * @param className
     * @return
     */
    private boolean innerSDKSkip(String className) {
        return (ClassNameAnalytics.isFTSdkPackage(className)
                && !ClassNameAnalytics.isFTSdkApi(className));
    }


    /**
     * Whether it is a third-party or internal WebView method
     *
     * @param className
     * @param superName
     * @param methodNameDesc
     * @return
     */
    private boolean isWebViewInner(String className, String superName, String methodNameDesc) {
        return (ClassNameAnalytics.isDCloud(className)
                || ClassNameAnalytics.isTencent(className)
                || ClassNameAnalytics.isTaoBao(className)
                || superName.equals(Constants.CLASS_NAME_WEBVIEW))
                && TARGET_WEBVIEW_METHOD.contains(methodNameDesc);
    }

}