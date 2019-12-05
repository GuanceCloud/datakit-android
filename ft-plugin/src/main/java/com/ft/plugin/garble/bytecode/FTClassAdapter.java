package com.ft.plugin.garble.bytecode;

import com.ft.plugin.garble.FTTransformHelper;
import com.ft.plugin.garble.FTUtil;
import com.ft.plugin.garble.Logger;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * BY huangDianHua
 * DATE:2019-11-29 15:15
 * Description:
 */
public class FTClassAdapter extends ClassVisitor {
    private String className;
    private String superName;
    private String[] interfaces;
    private FTTransformHelper ftTransformHelper;

    FTClassAdapter(final ClassVisitor cv, FTTransformHelper ftTransformHelper) {
        super(FTUtil.ASM_VERSION, cv);
        this.ftTransformHelper = ftTransformHelper;
        Logger.info(">>>> goon scan class ");
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

        Logger.info(">>>> end scan class：" + className + "<<<<");
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        FTMethodAdapter ftMethodAdapter = new FTMethodAdapter(mv, access, name, desc, className, interfaces, superName, ftTransformHelper);
        return ftMethodAdapter;
    }
}