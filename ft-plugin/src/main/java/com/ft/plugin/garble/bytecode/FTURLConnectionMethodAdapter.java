package com.ft.plugin.garble.bytecode;

import com.ft.plugin.garble.Constants;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Buffers one method to recognize an existing return-value hook on repeated transforms. */
final class FTURLConnectionMethodAdapter extends MethodNode {
    static final String HOOK = "com/ft/sdk/FTURLConnectionInstrumentation";
    static final String WRAP_DESC = "(Ljava/net/URLConnection;)Ljava/net/URLConnection;";
    private final MethodVisitor downstream;
    private boolean ignored;

    FTURLConnectionMethodAdapter(int api, int access, String name, String descriptor,
                                 String signature, String[] exceptions, MethodVisitor downstream) {
        super(api, access, name, descriptor, signature, exceptions);
        this.downstream = downstream;
    }

    @Override public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (Constants.IGNORE_ANNOTATION.equals(descriptor)) ignored = true;
        return super.visitAnnotation(descriptor, visible);
    }

    @Override public void visitEnd() {
        if (ignored) {
            accept(downstream);
            return;
        }
        for (AbstractInsnNode instruction = instructions.getFirst(); instruction != null;
             instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() != Opcodes.INVOKEVIRTUAL || call.itf
                    || !"java/net/URL".equals(call.owner) || !"openConnection".equals(call.name)
                    || !("()Ljava/net/URLConnection;".equals(call.desc)
                    || "(Ljava/net/Proxy;)Ljava/net/URLConnection;".equals(call.desc))) continue;
            AbstractInsnNode next = call.getNext();
            while (next != null && next.getOpcode() < 0) next = next.getNext();
            if (next instanceof MethodInsnNode) {
                MethodInsnNode existing = (MethodInsnNode) next;
                if (existing.getOpcode() == Opcodes.INVOKESTATIC && HOOK.equals(existing.owner)
                        && "wrap".equals(existing.name) && WRAP_DESC.equals(existing.desc)) continue;
            }
            instructions.insert(call, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "wrap", WRAP_DESC, false));
        }
        accept(downstream);
    }
}
