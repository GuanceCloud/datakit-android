package com.ft.plugin.garble.bytecode;

import com.ft.plugin.garble.Constants;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Buffers one method to recognize an existing return-value hook on repeated transforms. */
final class FTURLConnectionMethodAdapter extends MethodNode {
    static final String HOOK = "com/ft/sdk/FTURLConnectionInstrumentation";
    static final String WRAP_DESC = "(Ljava/net/URLConnection;Ljava/net/Proxy;)Ljava/net/URLConnection;";
    private static final String LEGACY_WRAP_DESC = "(Ljava/net/URLConnection;)Ljava/net/URLConnection;";
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
            // New hooks load the captured proxy (or null for the default overload).
            AbstractInsnNode afterProxy = next == null ? null : next.getNext();
            while (afterProxy != null && afterProxy.getOpcode() < 0) afterProxy = afterProxy.getNext();
            if (afterProxy instanceof MethodInsnNode && isHook((MethodInsnNode) afterProxy, WRAP_DESC)) continue;
            if (next instanceof MethodInsnNode && isHook((MethodInsnNode) next, LEGACY_WRAP_DESC)) {
                instructions.remove(next);
            }
            InsnList after = new InsnList();
            if (call.desc.startsWith("(Ljava/net/Proxy;")) {
                // Retain the original invocation and evaluate receiver/argument only once.
                int proxyLocal = maxLocals++;
                InsnList before = new InsnList();
                before.add(new InsnNode(Opcodes.DUP));
                before.add(new VarInsnNode(Opcodes.ASTORE, proxyLocal));
                instructions.insertBefore(call, before);
                after.add(new VarInsnNode(Opcodes.ALOAD, proxyLocal));
            } else {
                after.add(new InsnNode(Opcodes.ACONST_NULL));
            }
            after.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "wrap", WRAP_DESC, false));
            instructions.insert(call, after);
            maxStack += 1;
        }
        accept(downstream);
    }
    private static boolean isHook(MethodInsnNode call, String descriptor) {
        return call.getOpcode() == Opcodes.INVOKESTATIC && HOOK.equals(call.owner)
                && "wrap".equals(call.name) && descriptor.equals(call.desc);
    }
}
