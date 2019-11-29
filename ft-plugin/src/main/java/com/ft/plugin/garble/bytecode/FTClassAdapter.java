package com.ft.plugin.garble.bytecode;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;

/**
 * BY huangDianHua
 * DATE:2019-11-29 15:15
 * Description:
 */
public class FTClassAdapter extends ClassVisitor {

    private String className;

    FTClassAdapter(final ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        System.out.println("FTClassAdapter : scan class ----> " + className);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

        //匹配具有生命周期的类
        if ("onCreate".equals(name)) {
            //处理onCreate
            System.out.println("FTClassAdapter : change method ----> " + name);
            return new FTMethodAdapter(className + File.separator + name, access, desc, mv);
        } else if ("onDestroy".equals(name)) {
            //处理onDestroy
            System.out.println("FTClassAdapter : change method ----> " + name);
            return new FTMethodAdapter(className + File.separator + name, access, desc, mv);
        }else if("performClick".equals(name)){
            System.out.println("FTClassAdapter : change method ----> " + name);
            return new FTMethodAdapter(className + File.separator + name, access, desc, mv);
        }
        return mv;
    }


}