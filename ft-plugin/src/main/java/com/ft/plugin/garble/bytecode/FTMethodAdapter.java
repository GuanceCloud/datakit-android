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

import com.ft.plugin.garble.ClassNameAnalytics;
import com.ft.plugin.garble.FTHookConfig;
import com.ft.plugin.garble.FTMethodCell;
import com.ft.plugin.garble.FTMethodType;
import com.ft.plugin.garble.FTSubMethodCell;
import com.ft.plugin.garble.FTTransformHelper;
import com.ft.plugin.garble.FTUtil;
import com.ft.plugin.garble.Logger;
import com.ft.plugin.garble.VersionConfig;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * 本类借鉴修改了来自 Sensors Data 的项目 https://github.com/sensorsdata/sa-sdk-android-plugin2
 * 中的 SensorsAnalyticsClassVisitor.groovy 类
 */
public class FTMethodAdapter extends AdviceAdapter {
    private FTTransformHelper ftTransformHelper;
    private String[] interfaces;
    private String className;
    private String superName;
    private String methodName;
    private boolean isHasTracked = false;
    //name + desc
    private String nameDesc;

    //访问权限是public并且非静态
    private boolean pubAndNoStaticAccess;

    private int startVarIndex;

    public FTMethodAdapter(MethodVisitor mv, int access, String name, String desc, String className, String[] interfaces, String supperName, FTTransformHelper ftTransformHelper) {
        super(FTUtil.ASM_VERSION, mv, access, name, desc);
        this.methodName = name;
        this.superName = supperName;
        this.ftTransformHelper = ftTransformHelper;
        this.className = className;
        this.interfaces = interfaces;
        //Logger.info(">>>> 开始扫描类 <" + className + "> 的方法:" + methodName + "<<<<");
    }

    /**
     * 判断当前的类中的方法是否需要统计时长（如果后期需要统计更多的方法可以扩展该方法）
     * @return
     */
    private boolean needTrackTime(){
        if((superName.equals("androidx/appcompat/app/AppCompatActivity") ||
                superName.equals("android/app/Activity")) && !className.startsWith("android/") && !className.startsWith("androidx/") && (methodName+methodDesc).equals("onCreate(Landroid/os/Bundle;)V")){
            return true;
        }
        return false;
    }
    @Override
    public void visitCode() {
        super.visitCode();
        if(needTrackTime()) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            startVarIndex = newLocal(Type.LONG_TYPE);
            mv.visitVarInsn(Opcodes.LSTORE, startVarIndex);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if(needTrackTime()) {
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                mv.visitVarInsn(LLOAD, startVarIndex);
                mv.visitInsn(LSUB);
                int index = newLocal(Type.LONG_TYPE);
                mv.visitVarInsn(LSTORE, index);
                mv.visitLdcInsn(className + "|" + methodName + "|" + methodDesc);
                mv.visitVarInsn(LLOAD, index);
                mv.visitMethodInsn(INVOKESTATIC, "com/ft/sdk/FTAutoTrack", "timingMethod", "(Ljava/lang/String;J)V", false);
            }
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (isHasTracked) {
            FTHookConfig.mLambdaMethodCells.remove(nameDesc);
            Logger.info("Hooked Class<" + className + ">的 method: " + methodName + "," + methodDesc);
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String name1, String desc1, Handle bsm, Object... bsmArgs) {
        super.visitInvokeDynamicInsn(name1, desc1, bsm, bsmArgs);
        try {
            Object object = bsmArgs[0];
            String desc2="";
            if(object instanceof Type){
                desc2 = ((Type) object).getDescriptor();
            }
            FTMethodCell ftMethodCell = FTHookConfig.LAMBDA_METHODS.get(Type.getReturnType(desc1).getDescriptor()+name1+desc2);
            if(ftMethodCell != null){
                Handle it = (Handle) bsmArgs[1];
                FTHookConfig.mLambdaMethodCells.put(it.getName()+it.getDesc(),ftMethodCell);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        //这部分为替换使用的系统Log
        if("android/util/Log".equals(owner)) {
            if("i".equals(name)) {
                if("(Ljava/lang/String;Ljava/lang/String;)I".equals(desc)) {
                    mv.visitMethodInsn(INVOKESTATIC, "com/ft/sdk/garble/utils/TrackLog", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false);
                } else if("(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I".equals(desc)) {
                    mv.visitMethodInsn(INVOKESTATIC, "com/ft/sdk/garble/utils/TrackLog", "i", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I", false);
                } else {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            } else if("d".equals(name)) {
                if("(Ljava/lang/String;Ljava/lang/String;)I".equals(desc)) {
                    
                    mv.visitMethodInsn(INVOKESTATIC, "com/ft/sdk/garble/utils/TrackLog", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
                } else if("(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I".equals(desc)) {
                    
                    mv.visitMethodInsn(INVOKESTATIC, "com/ft/sdk/garble/utils/TrackLog", "d", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I", false);
                } else {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            } else if("v".equals(name)) {
                if("(Ljava/lang/String;Ljava/lang/String;)I".equals(desc)) {
                    
                    mv.visitMethodInsn(INVOKESTATIC, "com/ft/sdk/garble/utils/TrackLog", "v", "(Ljava/lang/String;Ljava/lang/String;)I", false);
                } else if("(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I".equals(desc)) {
                    
                    mv.visitMethodInsn(INVOKESTATIC, "com/ft/sdk/garble/utils/TrackLog", "v", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I", false);
                } else {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            } else if("e".equals(name)) {
                if("(Ljava/lang/String;Ljava/lang/String;)I".equals(desc)) {
                    
                    mv.visitMethodInsn(INVOKESTATIC, "com/ft/sdk/garble/utils/TrackLog", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false);
                } else if("(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I".equals(desc)) {
                    
                    mv.visitMethodInsn(INVOKESTATIC, "com/ft/sdk/garble/utils/TrackLog", "e", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I", false);
                } else {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            } else if("w".equals(name)) {
                if("(Ljava/lang/String;Ljava/lang/String;)I".equals(desc)) {
                    
                    mv.visitMethodInsn(INVOKESTATIC, "com/ft/sdk/garble/utils/TrackLog", "w", "(Ljava/lang/String;Ljava/lang/String;)I", false);
                } else if("(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I".equals(desc)) {
                    
                    mv.visitMethodInsn(INVOKESTATIC, "com/ft/sdk/garble/utils/TrackLog", "w", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I", false);
                } else if("(Ljava/lang/String;Ljava/lang/Throwable;)I".equals(desc)) {
                    
                    mv.visitMethodInsn(INVOKESTATIC, "com/ft/sdk/garble/utils/TrackLog", "w", "(Ljava/lang/String;Ljava/lang/Throwable;)I", false);
                } else {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            } else if("println".equals(name)) {
                if("(ILjava/lang/String;Ljava/lang/String;)I".equals(desc)) {
                    
                    mv.visitMethodInsn(INVOKESTATIC, "com/ft/sdk/garble/utils/TrackLog", "println", "(ILjava/lang/String;Ljava/lang/String;)I", false);
                } else {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        nameDesc = methodName + methodDesc;
        pubAndNoStaticAccess = FTUtil.isPublic(methodAccess) && !FTUtil.isStatic(methodAccess);
        if (ftTransformHelper.extension.openAutoTrack) {
            handleCode();
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
    }

    void handleCode(FTMethodCell ftMethodCell) {
        if (ftMethodCell.subMethodCellList != null && !ftMethodCell.subMethodCellList.isEmpty()) {
            for (FTSubMethodCell f : ftMethodCell.subMethodCellList) {
                if (f.type == FTMethodType.ALOAD) {
                    mv.visitVarInsn(ALOAD, f.value);
                } else if (f.type == FTMethodType.ILOAD) {
                    mv.visitVarInsn(ILOAD, f.value);
                } else if (f.type == FTMethodType.INVOKEVIRTUAL) {
                    mv.visitMethodInsn(INVOKEVIRTUAL, f.className, f.agentName, f.agentDesc, f.itf);
                } else if (f.type == FTMethodType.INVOKESPECIAL) {
                    mv.visitMethodInsn(INVOKESPECIAL, f.className, f.agentName, f.agentDesc, f.itf);
                }else if(f.type == FTMethodType.GETSTATIC){
                    mv.visitFieldInsn(GETSTATIC,f.className,f.agentName,f.agentDesc);
                }else if(f.type == FTMethodType.GETFIELD){
                    mv.visitFieldInsn(GETFIELD,f.className,f.agentName,f.agentDesc);
                } else if(f.type == FTMethodType.INVOKESTATIC){
                    mv.visitMethodInsn(INVOKESTATIC,f.className,f.agentName,f.agentDesc,f.itf);
                }
            }
        }
        mv.visitMethodInsn(INVOKESTATIC, FTHookConfig.FT_SDK_API, ftMethodCell.agentName, ftMethodCell.agentDesc, false);
    }

    void handleCode() {
        if (FTUtil.isTargetClassInSpecial(className)) {
            return;
        }

        if(ClassNameAnalytics.isFTSdkApi(className.replaceAll("/","."))){
            if(nameDesc.equals("install(Lcom/ft/sdk/FTSDKConfig;)V")){
                mv.visitLdcInsn(VersionConfig.PLUGIN_VERSION);
                mv.visitFieldInsn(PUTSTATIC, "com/ft/sdk/FTSdk", "PLUGIN_VERSION", "Ljava/lang/String;");
                isHasTracked = true;
                return;
            }
        }

        /**
         * androidx/fragment/app/Fragment，androidx/fragment/app/ListFragment，androidx/fragment/app/DialogFragment
         */
        if (FTUtil.isInstanceOfXFragment(className)) {
            //Logger.info("方法扫描>>>类是FragmentX>>>方法是"+nameDesc);
            FTMethodCell ftMethodCell = FTHookConfig.FRAGMENT_X_METHODS.get(nameDesc);
            if (ftMethodCell != null) {
                handleCode(ftMethodCell);
                isHasTracked = true;
                return;
            }
        }

        /**
         * android/support/v4/app/Fragment，android/support/v4/app/ListFragment，android/support/v4/app/DialogFragment，
         */
        if (FTUtil.isInstanceOfV4Fragment(className)) {
            //Logger.info("方法扫描>>>类是FragmentV4>>>方法是"+nameDesc);
            FTMethodCell ftMethodCell = FTHookConfig.FRAGMENT_V4_METHODS.get(nameDesc);
            if (ftMethodCell != null) {
                handleCode(ftMethodCell);
                isHasTracked = true;
                return;
            }
        }

        /**
         * android/app/Fragment，android/app/ListFragment， android/app/DialogFragment，
         */
        if (FTUtil.isInstanceOfFragment(className)) {
            //Logger.info("方法扫描>>>类是Fragment>>>方法是"+nameDesc);
            FTMethodCell ftMethodCell = FTHookConfig.FRAGMENT_METHODS.get(nameDesc);
            if (ftMethodCell != null) {
                handleCode(ftMethodCell);
                isHasTracked = true;
                return;
            }
        }

        /**
         * Hook Activity
         */
        if (FTUtil.isInstanceOfActivity(className)) {
            FTMethodCell ftMethodCell = FTHookConfig.ACTIVITY_METHODS.get(nameDesc);
            //Logger.info("方法扫描>>>类是Activity>>>方法是"+nameDesc+" ftMethodCell="+ftMethodCell);
            if (ftMethodCell != null) {
                handleCode(ftMethodCell);
                isHasTracked = true;
                return;
            }
        }

        /**
         * Hook Lambda 表达式
         */
        FTMethodCell lambdaMethodCell = FTHookConfig.mLambdaMethodCells.get(nameDesc);
        if(lambdaMethodCell != null){
            Type[] types = Type.getArgumentTypes(lambdaMethodCell.desc);
            int length = types.length;
            Type[] lambdaTypes = Type.getArgumentTypes(methodDesc);
            int paramStart = lambdaTypes.length - length;
            if (paramStart < 0){
                return;
            }else{
                for (int i = 0;i < length;i++){
                    if(!lambdaTypes[paramStart + i].getDescriptor().equals(types[i].getDescriptor())){
                        return;
                    }
                }
            }
            boolean isStaticMethod = FTUtil.isStatic(methodAccess);
            if(!isStaticMethod){
                if("(Landroid/view/MenuItem;)Z".equals(lambdaMethodCell.desc)){
                    mv.visitVarInsn(Opcodes.ALOAD,0);
                    mv.visitVarInsn(Opcodes.ALOAD,getVisitPosition(lambdaTypes,paramStart,isStaticMethod));
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC,FTHookConfig.FT_SDK_API,lambdaMethodCell.agentName,"(Ljava/lang/Object;Landroid/view/MenuItem;)V", false);
                    isHasTracked = true;
                    return;
                }
            }
            for (int i = paramStart;i< paramStart+lambdaMethodCell.paramsCount;i++){
                mv.visitVarInsn(lambdaMethodCell.opcodes.get(i-paramStart),getVisitPosition(lambdaTypes,i,isStaticMethod));
            }

            mv.visitMethodInsn(Opcodes.INVOKESTATIC,FTHookConfig.FT_SDK_API,lambdaMethodCell.agentName,lambdaMethodCell.agentDesc,false);
            isHasTracked = true;
            return;
        }

        if (!pubAndNoStaticAccess) {
            //Logger.info("方法扫描>>>类是" + className + ">>>方法是" + nameDesc + " 静态和非公共方法");
            return;
        }

        if ((className.startsWith("android/") || className.startsWith("androidx/")) && !(className.startsWith("android/support/v17/leanback") || className.startsWith("androidx/leanback"))) {
            return;
        }

        /**
         * 系统控件点击事件
         */
        if(interfaces != null && interfaces.length >0){
            for(String inter :interfaces) {
                //Logger.info("============CLICK_METHODS_SYSTEM=="+inter+nameDesc);
                FTMethodCell ftMethodCell = FTHookConfig.CLICK_METHODS_SYSTEM.get(inter+nameDesc);
                if (ftMethodCell != null) {
                    handleCode(ftMethodCell);
                    isHasTracked = true;
                    return;
                }
            }
        }

        /**
         * 支持 onContextItemSelected(MenuItem item)、onOptionsItemSelected(MenuItem item)、onNavigationItemSelected(MenuItem item)
         */
        if (FTUtil.isTargetMenuMethodDesc(nameDesc)) {
            handleCode(FTHookConfig.MENU_METHODS);
            return;
        }

        if (methodDesc.equals("(Landroid/view/View;)V")) {
            //Logger.info("方法扫描>>>类是" + className + ">>>方法是" + nameDesc + " 点击");
            handleCode(FTHookConfig.CLICK_METHOD);
            isHasTracked = true;
            return;
        }
    }

    /**
     * 获取方法参数下标为 index 的对应 ASM index
     * @param types 方法参数类型数组
     * @param index 方法中参数下标，从 0 开始
     * @param isStaticMethod 该方法是否为静态方法
     * @return 访问该方法的 index 位参数的 ASM index
     */
    int getVisitPosition(Type[] types, int index, boolean isStaticMethod) {
        if (types == null || index < 0 || index >= types.length) {
            throw new Error("getVisitPosition error");
        }
        if (index == 0) {
            return isStaticMethod ? 0 : 1;
        } else {
            return getVisitPosition(types, index - 1, isStaticMethod) + types[index - 1].getSize();
        }
    }
}

