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

import com.ft.plugin.garble.FTTransformHelper;
import com.ft.plugin.garble.FTUtil;
import com.ft.plugin.garble.Logger;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * 本类借鉴修改了来自 Sensors Data 的项目 https://github.com/sensorsdata/sa-sdk-android-plugin2
 * 中的 SensorsAnalyticsClassVisitor.groovy 类
 */
public class FTClassAdapter extends ClassVisitor {
    private String className;
    private String superName;
    private String[] interfaces;
    private FTTransformHelper ftTransformHelper;

    FTClassAdapter(final ClassVisitor cv, FTTransformHelper ftTransformHelper) {
        super(FTUtil.ASM_VERSION, cv);
        this.ftTransformHelper = ftTransformHelper;
        //Logger.info(">>>> goon scan class ");
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        this.superName = superName;
        this.interfaces = interfaces;
        Logger.info(">>>> start scan class ----> " + className + ", superName=" + superName);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

        //Logger.info(">>>> end scan class：" + className + "<<<<");
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        FTMethodAdapter ftMethodAdapter = new FTMethodAdapter(mv, access, name, desc, className, interfaces, superName, ftTransformHelper);
        return ftMethodAdapter;
    }
}