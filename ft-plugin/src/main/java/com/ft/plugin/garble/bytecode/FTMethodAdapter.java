package com.ft.plugin.garble.bytecode;

import com.ft.plugin.garble.FTHookConfig;
import com.ft.plugin.garble.FTMethodCell;
import com.ft.plugin.garble.FTMethodType;
import com.ft.plugin.garble.FTSubMethodCell;
import com.ft.plugin.garble.FTTransformHelper;
import com.ft.plugin.garble.FTUtil;
import com.ft.plugin.garble.Logger;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * BY huangDianHua
 * DATE:2019-11-29 15:04
 * Description:
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

    public FTMethodAdapter(MethodVisitor mv, int access, String name, String desc, String className, String[] interfaces, String supperName, FTTransformHelper ftTransformHelper) {
        super(FTUtil.ASM_VERSION, mv, access, name, desc);
        this.methodName = name;
        this.superName = supperName;
        this.ftTransformHelper = ftTransformHelper;
        this.className = className;
        this.interfaces = interfaces;
        Logger.info(">>>> 开始扫描类 <" + className + "> 的方法:" + methodName + "<<<<");
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (isHasTracked) {
            Logger.info("Hooked Class<" + className + ">的 method: " + methodName + "," + methodDesc);
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String name1, String desc1, Handle bsm, Object... bsmArgs) {
        super.visitInvokeDynamicInsn(name1, desc1, bsm, bsmArgs);
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        nameDesc = methodName + methodDesc;
        pubAndNoStaticAccess = FTUtil.isPublic(methodAccess) && !FTUtil.isStatic(methodAccess);
        if (ftTransformHelper.extension.canHookMethod) {
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
                }
            }
        }
        mv.visitMethodInsn(INVOKESTATIC, FTHookConfig.FT_SDK_API, ftMethodCell.agentName, ftMethodCell.agentDesc, false);
    }

    void handleCode() {
        /**
         * 写Application方法
         */
        if (FTUtil.isInstanceOfApplication(superName)) {
            FTMethodCell ftMethodCell = FTHookConfig.APPLICATION_METHODS.get(nameDesc);
            if (ftMethodCell != null) {
                handleCode(ftMethodCell);
                isHasTracked = true;
                return;
            }
        }
        /**
         * androidx/fragment/app/Fragment，androidx/fragment/app/ListFragment，androidx/fragment/app/DialogFragment
         */
        if (FTUtil.isInstanceOfXFragment(superName)) {
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
        if (FTUtil.isInstanceOfV4Fragment(superName)) {
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
        if (FTUtil.isInstanceOfFragment(superName)) {
            //Logger.info("方法扫描>>>类是Fragment>>>方法是"+nameDesc);
            FTMethodCell ftMethodCell = FTHookConfig.FRAGMENT_METHODS.get(nameDesc);
            if (ftMethodCell != null) {
                handleCode(ftMethodCell);
                isHasTracked = true;
                return;
            }
        }

        if (FTUtil.isInstanceOfActivity(superName)) {
            FTMethodCell ftMethodCell = FTHookConfig.ACTIVITY_METHODS.get(nameDesc);
            //Logger.info("方法扫描>>>类是Activity>>>方法是"+nameDesc+" ftMethodCell="+ftMethodCell);
            if (ftMethodCell != null) {
                handleCode(ftMethodCell);
                isHasTracked = true;
                return;
            }
        }

        if (!pubAndNoStaticAccess) {
            Logger.info("方法扫描>>>类是" + className + ">>>方法是" + nameDesc + " 静态和非公共方法");
            return;
        }

        /**
         * 支持 onContextItemSelected(MenuItem item)、onOptionsItemSelected(MenuItem item)、onNavigationItemSelected(MenuItem item)
         */
        if (FTUtil.isTargetMenuMethodDesc(nameDesc)) {
            handleCode(FTHookConfig.MENU_METHODS);
            return;
        }


        if (!FTUtil.isTargetClassInSpecial(className)) {
            if ((className.startsWith("android/") || className.startsWith("androidx/")) && !(className.startsWith("android/support/v17/leanback") || className.startsWith("androidx/leanback"))) {
                return;
            }
        }

        if (methodDesc.equals("(Landroid/view/View;)V")) {
            Logger.info("方法扫描>>>类是" + className + ">>>方法是" + nameDesc + " 点击");
            handleCode(FTHookConfig.CLICK_METHOD);
            isHasTracked = true;
            return;
        }
    }
}

