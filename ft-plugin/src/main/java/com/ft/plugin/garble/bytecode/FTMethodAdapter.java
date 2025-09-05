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
import com.ft.plugin.garble.FTHookConfig;
import com.ft.plugin.garble.FTMethodCell;
import com.ft.plugin.garble.FTMethodType;
import com.ft.plugin.garble.FTSubMethodCell;
import com.ft.plugin.garble.FTUtil;
import com.ft.plugin.garble.Logger;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * This class is adapted and modified from the Sensors Data project
 * <a href="https://github.com/sensorsdata/sa-sdk-android-plugin2">sa-sdk-android-plugin2</a>
 * SensorsAnalyticsClassVisitor.groovy class
 * Visit class method structure
 */
public class FTMethodAdapter extends AdviceAdapter {
    private final String[] interfaces;

    /**
     * Visit class name
     */
    private final String className;
    /**
     * Visit superclass
     */
    private final String superName;
    /**
     * Method name
     */
    private final String methodName;
    /**
     * Whether it has been written
     */
    private boolean isHasTracked = false;
    /**
     * Whether to skip
     */
    private boolean needSkip = false;
    /**
     * name + desc
     */
    private final String nameDesc;

    /**
     * Access permission is public and non-static
     */
    private boolean pubAndNoStaticAccess;

    public FTMethodAdapter(MethodVisitor mv, int access, String name, String desc, String className,
                           String[] interfaces, String superName, int api) {
        super(api, mv, access, name, desc);
        this.methodName = name;
        this.superName = superName;
        this.className = className;
        this.interfaces = interfaces;
        this.nameDesc = name + desc;
    }

    @Override
    public void visitCode() {
        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (isHasTracked) {
            FTHookConfig.mLambdaMethodCells.remove(nameDesc);
            if (!needSkip) {
                Logger.debug("Hooked Class<" + className + "> method: " + methodName + ", desc:" + methodDesc);
            }
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String name1, String desc1, Handle bsm, Object... bsmArgs) {
        super.visitInvokeDynamicInsn(name1, desc1, bsm, bsmArgs);
        try {
            Object object = bsmArgs[0];
            String desc2 = "";
            if (object instanceof Type) {
                desc2 = ((Type) object).getDescriptor();
            }
            FTMethodCell ftMethodCell = FTHookConfig.LAMBDA_METHODS.get(Type.getReturnType(desc1).getDescriptor() + name1 + desc2);
            if (ftMethodCell != null) {
                Handle it = (Handle) bsmArgs[1];
                FTHookConfig.mLambdaMethodCells.put(it.getName() + it.getDesc(), ftMethodCell);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * All loading methods contained in WebView
     */
    static final List<String> TARGET_WEBVIEW_METHOD = Arrays.asList("loadUrl(Ljava/lang/String;)V",
            "loadUrl(Ljava/lang/String;Ljava/util/Map;)V",
            "loadData(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
            "loadDataWithBaseURL(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
            "postUrl(Ljava/lang/String;[B)V");

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (needSkip) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }
        switch (owner) {
            case Constants.CLASS_NAME_HTTP_CLIENT_BUILDER:
                if ("build()Lorg/apache/hc/client5/http/impl/classic/CloseableHttpClient;".contains(name + desc)) {
                    mv.visitMethodInsn(INVOKESTATIC, Constants.FT_SDK_HOOK_CLASS, "trackHttpClientBuilder",
                            "(Lorg/apache/hc/client5/http/impl/classic/HttpClientBuilder;)" +
                                    "Lorg/apache/hc/client5/http/impl/classic/CloseableHttpClient;",
                            false);
                    Logger.debug("CLASS_NAME_HTTP_CLIENT_BUILDER-> owner:" + owner + ", class:" + className
                            + ", super:" + superName + ", method:" + name + desc + " | " + nameDesc);
                    return;
                }
                break;

            case Constants.CLASS_NAME_OKHTTP_BUILDER:
                if ("build()Lokhttp3/OkHttpClient;".contains(name + desc)) {
                    mv.visitMethodInsn(INVOKESTATIC, Constants.FT_SDK_HOOK_CLASS, "trackOkHttpBuilder",
                            "(Lokhttp3/OkHttpClient$Builder;)Lokhttp3/OkHttpClient;",
                            false);

                    Logger.debug("CLASS_NAME_OKHTTP_BUILDER-> owner:" + owner + ", class:" + className
                            + ", super:" + superName + ", method:" + name + desc + " | " + nameDesc);
                    return;
                }
                break;
            case Constants.CLASS_NAME_REQUEST_BUILDER:
                if ("build()Lokhttp3/Request;".contains(name + desc)) {
                    mv.visitMethodInsn(INVOKESTATIC, Constants.FT_SDK_HOOK_CLASS, "trackRequestBuilder",
                            "(Lokhttp3/Request$Builder;)Lokhttp3/Request;",
                            false);
                    Logger.debug("CLASS_NAME_REQUEST_BUILDER-> owner:" + owner + ", class:" + className
                            + ", super:" + superName + ", method:" + name + desc + " | " + nameDesc);
                    return;
                }
                break;
            case Constants.CLASS_NAME_WEBVIEW:
            case Constants.CLASS_NAME_RN_WEBVIEW:
            case Constants.CLASS_NAME_TENCENT_WEBVIEW:
                String method = name + desc;
                if (TARGET_WEBVIEW_METHOD.contains(method)) {
                    if (nameDesc.startsWith(Constants.INNER_CLASS_METHOD_PREFIX)) {
                        Logger.debug("WebInner Ignore-> owner:" + owner + ", class:" + className
                                + ", super:" + superName + ", method:" + nameDesc);
                    } else {
                        Logger.debug("TARGET_WEBVIEW_METHOD-> owner:" + owner + ", class:" + className
                                + ", super:" + superName + ", method:" + method + " | " + nameDesc);
                        mv.visitMethodInsn(INVOKESTATIC, Constants.FT_SDK_HOOK_CLASS, name,
                                desc.replaceFirst("\\(", "(" + Constants.VIEW_DESC), itf);
                        return;
                    }
                }
                break;

            case Constants.CLASS_NAME_LOG:

                if (ClassNameAnalytics.isAndroidPackage(className) || ClassNameAnalytics.isFTSdkApi(className)) {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                    return;
                }

                switch (name) {
                    case "i":
                    case "d":
                    case "v":
                    case "e":
                        if (Constants.METHOD_DESC_S_S_I.equals(desc)) {
                            mv.visitMethodInsn(INVOKESTATIC, Constants.CLASS_NAME_TRACKLOG, name,
                                    Constants.METHOD_DESC_S_S_I, false);
                        } else if (Constants.METHOD_DESC_S_S_T_I.equals(desc)) {
                            mv.visitMethodInsn(INVOKESTATIC, Constants.CLASS_NAME_TRACKLOG, name,
                                    Constants.METHOD_DESC_S_S_T_I, false);
                        } else {
                            super.visitMethodInsn(opcode, owner, name, desc, itf);
                        }
                        return;
                    case "w":
                        if (Constants.METHOD_DESC_S_S_I.equals(desc)) {
                            mv.visitMethodInsn(INVOKESTATIC, Constants.CLASS_NAME_TRACKLOG, "w",
                                    Constants.METHOD_DESC_S_S_I, false);
                        } else if (Constants.METHOD_DESC_S_S_T_I.equals(desc)) {
                            mv.visitMethodInsn(INVOKESTATIC, Constants.CLASS_NAME_TRACKLOG, "w",
                                    Constants.METHOD_DESC_S_S_T_I, false);
                        } else if (Constants.METHOD_DESC_S_T_I.equals(desc)) {
                            mv.visitMethodInsn(INVOKESTATIC, Constants.CLASS_NAME_TRACKLOG, "w",
                                    Constants.METHOD_DESC_S_T_I, false);
                        } else {
                            super.visitMethodInsn(opcode, owner, name, desc, itf);
                        }
                        return;

                    case "println":
                        if (Constants.METHOD_DESC_I_S_S_I.equals(desc)) {

                            mv.visitMethodInsn(INVOKESTATIC, Constants.CLASS_NAME_TRACKLOG,
                                    "println", Constants.METHOD_DESC_I_S_S_I, false);

                        } else {
                            super.visitMethodInsn(opcode, owner, name, desc, itf);
                        }
                        return;

                    default:
                        break;
                }
                break;

            default:
                break;
        }

        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        pubAndNoStaticAccess = FTUtil.isPublic(methodAccess) && !FTUtil.isStatic(methodAccess);
        handleCode();
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
    }

    /**
     * Modify the common way of modifying method content
     *
     * @param ftMethodCell
     */
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
                } else if (f.type == FTMethodType.GETSTATIC) {
                    mv.visitFieldInsn(GETSTATIC, f.className, f.agentName, f.agentDesc);
                } else if (f.type == FTMethodType.GETFIELD) {
                    mv.visitFieldInsn(GETFIELD, f.className, f.agentName, f.agentDesc);
                } else if (f.type == FTMethodType.INVOKESTATIC) {
                    mv.visitMethodInsn(INVOKESTATIC, f.className, f.agentName, f.agentDesc, f.itf);
                }
            }
        }
    }

    void handleCode() {
        if (needSkip) return;
        /*
         * Write Application method
         */
        if (FTUtil.isInstanceOfApplication(superName)) {
            FTMethodCell ftMethodCell = FTHookConfig.APPLICATION_METHODS.get(nameDesc);
            if (ftMethodCell != null) {
                handleCode(ftMethodCell);
                isHasTracked = true;
                return;
            }
        }

        if (ClassNameAnalytics.isFTSdkApi(className)) {
            if (nameDesc.equals("install(Lcom/ft/sdk/FTSDKConfig;)V")) {
                mv.visitLdcInsn(BuildConfig.PLUGIN_VERSION);
                mv.visitFieldInsn(PUTSTATIC, "com/ft/sdk/FTSdk", "PLUGIN_VERSION",
                        "Ljava/lang/String;");

                mv.visitLdcInsn(Constants.PACKAGE_UUID);
                mv.visitFieldInsn(PUTSTATIC, "com/ft/sdk/FTSdk", "PACKAGE_UUID",
                        "Ljava/lang/String;");

                isHasTracked = true;
                return;
            }
        }

        /**
         * androidx/fragment/app/Fragment，androidx/fragment/app/ListFragment，androidx/fragment/app/DialogFragment
         */
        if (FTUtil.isInstanceOfXFragment(className)) {
            //Logger.info("Method scan>>>Class is FragmentX>>>Method is "+nameDesc);
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
            //Logger.info("Method scan>>>Class is FragmentV4>>>Method is "+nameDesc);
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
            //Logger.info("Method scan>>>Class is Fragment>>>Method is "+nameDesc);
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
            //Logger.info("Method scan>>>Class is Activity>>>Method is "+nameDesc+" ftMethodCell="+ftMethodCell);
            if (ftMethodCell != null) {
                handleCode(ftMethodCell);
                isHasTracked = true;
                return;
            }
        }

        /**
         * Hook Lambda expression
         */
        FTMethodCell lambdaMethodCell = FTHookConfig.mLambdaMethodCells.get(nameDesc);
        if (lambdaMethodCell != null) {
            Type[] types = Type.getArgumentTypes(lambdaMethodCell.desc);
            int length = types.length;
            Type[] lambdaTypes = Type.getArgumentTypes(methodDesc);
            int paramStart = lambdaTypes.length - length;
            if (paramStart < 0) {
                return;
            } else {
                for (int i = 0; i < length; i++) {
                    if (!lambdaTypes[paramStart + i].getDescriptor().equals(types[i].getDescriptor())) {
                        return;
                    }
                }
            }
            boolean isStaticMethod = FTUtil.isStatic(methodAccess);
            if (!isStaticMethod) {
                if ("(Landroid/view/MenuItem;)Z".equals(lambdaMethodCell.desc)) {
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitVarInsn(Opcodes.ALOAD, getVisitPosition(lambdaTypes, paramStart, isStaticMethod));
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.FT_SDK_HOOK_CLASS,
                            lambdaMethodCell.agentName, "(Ljava/lang/Object;Landroid/view/MenuItem;)V",
                            false);
                    isHasTracked = true;
                    return;
                }
            }
            for (int i = paramStart; i < paramStart + lambdaMethodCell.paramsCount; i++) {
                mv.visitVarInsn(lambdaMethodCell.opcodes.get(i - paramStart), getVisitPosition(lambdaTypes,
                        i, isStaticMethod));
            }

            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.FT_SDK_HOOK_CLASS, lambdaMethodCell.agentName,
                    lambdaMethodCell.agentDesc, false);
            isHasTracked = true;
            return;
        }

        if (!pubAndNoStaticAccess) {
            //Logger.info("Method scan>>>Class is " + className + ">>>Method is " + nameDesc + " static and non-public method");
            return;
        }

        if ((className.startsWith("android/") || className.startsWith("androidx/")) &&
                !(className.startsWith("android/support/v17/leanback")
                        || className.startsWith("androidx/leanback"))) {
            return;
        }

        /**
         * System component click event
         */
        if (interfaces != null && interfaces.length > 0) {
            for (String inter : interfaces) {
                //Logger.info("============CLICK_METHODS_SYSTEM=="+inter+nameDesc);
                FTMethodCell ftMethodCell = FTHookConfig.CLICK_METHODS_SYSTEM.get(inter + nameDesc);
                if (ftMethodCell != null) {
                    handleCode(ftMethodCell);
                    isHasTracked = true;
                    return;
                }
            }
        }

        /**
         * Support onContextItemSelected(MenuItem item)、
         * onOptionsItemSelected(MenuItem item)、
         * onNavigationItemSelected(MenuItem item)
         */
        if (FTUtil.isTargetMenuMethodDesc(nameDesc)) {
            handleCode(FTHookConfig.MENU_METHODS);
            isHasTracked = true;
            return;
        }

        if (methodDesc.equals("(Landroid/view/View;)V")) {
            //Logger.info("Method scan>>>Class is " + className + ">>>Method is " + nameDesc + " click");
            handleCode(FTHookConfig.CLICK_METHOD);
            isHasTracked = true;
            return;
        }
    }

    /**
     * Get the ASM index of the parameter at index in the method parameter array
     *
     * @param types           Method parameter type array
     * @param index           Method parameter index, starting from 0
     * @param isStaticMethod  Whether the method is a static method
     * @return ASM index of the parameter at index in the method parameter array
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


    /**
     * Annotation access
     *
     * @param descriptor the class descriptor of the annotation class.
     * @param visible    {@literal true} if the annotation is visible at runtime.
     * @return
     */
    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (Constants.IGNORE_ANNOTATION.equals(descriptor)) {
            Logger.debug("ignoreAOP-> class:" + className + ",super:" + superName + ", method:" + methodName);
            needSkip = true;
            return null;
        }
        return super.visitAnnotation(descriptor, visible);
    }
}

