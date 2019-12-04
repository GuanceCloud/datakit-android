package com.ft.plugin.garble.bytecode;

import com.ft.plugin.garble.FTHookConfig;
import com.ft.plugin.garble.FTMethodCell;
import com.ft.plugin.garble.FTTransformHelper;
import com.ft.plugin.garble.FTUtil;
import com.ft.plugin.garble.Logger;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * BY huangDianHua
 * DATE:2019-11-29 15:15
 * Description:
 */
public class FTClassAdapter extends ClassVisitor {
    private ClassVisitor classVisitor;
    private String className;
    private String superName;
    private String[] interfaces;
    private FTTransformHelper ftTransformHelper;
    private HashSet<String> visitedFragMethods = new HashSet<>();
    private ArrayList<FTMethodCell> methodCells = new ArrayList<>();

    FTClassAdapter(final ClassVisitor cv, FTTransformHelper ftTransformHelper) {
        super(FTUtil.ASM_VERSION, cv);
        this.classVisitor = cv;
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

        Logger.info(">>>> end scan class："+className+"<<<<");
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        FTMethodAdapter ftMethodAdapter = new FTMethodAdapter(mv,access,name,desc,className,interfaces,superName,visitedFragMethods,ftTransformHelper);
        return ftMethodAdapter;
    }

    private static void visitMethodWithLoadedParams(MethodVisitor methodVisitor, int opcode, String owner, String methodName, String methodDesc, int start, int count, List<Integer> paramOpcodes) {
        for (int i = start; i < start + count; i++) {
            methodVisitor.visitVarInsn(paramOpcodes.get(i - start), i);
        }
        methodVisitor.visitMethodInsn(opcode, owner, methodName, methodDesc, false);
    }

}