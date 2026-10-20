package com.ft.plugin.garble.bytecode;

import com.ft.plugin.garble.asm.FTParameters;
import com.ft.plugin.BuildConfig;
import org.gradle.api.tasks.Input;
import org.junit.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.util.Collections;
import static org.junit.Assert.*;

public class FTURLConnectionInstrumentationTest {
    private byte[] fixture(String owner, String name, String desc) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "sample/Calls", null, "java/lang/Object", null);
        MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "call",
                "(Ljava/net/URL;Ljava/net/Proxy;)Ljava/net/URLConnection;", null, new String[]{"java/io/IOException"});
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        if (desc.startsWith("(Ljava/net/Proxy;")) method.visitVarInsn(Opcodes.ALOAD, 1);
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, name, desc, false);
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(2, 2);
        method.visitEnd(); writer.visitEnd();
        return writer.toByteArray();
    }

    private byte[] transform(byte[] input, boolean enabled, boolean excluded) {
        ClassWriter writer = new ClassWriter(0);
        new ClassReader(input).accept(new FTClassAdapter(writer, Opcodes.ASM9,
                excluded ? Collections.singletonList("sample.**") : Collections.emptyList(),
                false, Collections.emptyList(), enabled), 0);
        return writer.toByteArray();
    }

    private int count(byte[] bytes, String owner, String name) {
        ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0);
        int count = 0;
        for (MethodNode method : node.methods) for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) insn;
                if (owner.equals(call.owner) && name.equals(call.name)) count++;
            }
        }
        return count;
    }

    @Test public void bothOverloadsRetainExactlyOneOriginalCallAndOneHookAfterRepeatedTransforms() {
        for (String desc : new String[]{"()Ljava/net/URLConnection;", "(Ljava/net/Proxy;)Ljava/net/URLConnection;"}) {
            byte[] transformed = transform(fixture("java/net/URL", "openConnection", desc), true, false);
            transformed = transform(transformed, true, false);
            assertEquals(1, count(transformed, "java/net/URL", "openConnection"));
            assertEquals(1, count(transformed, FTURLConnectionMethodAdapter.HOOK, "wrap"));
        }
    }

    @Test public void disabledAndExcludedClassesHaveNoHook() {
        byte[] input = fixture("java/net/URL", "openConnection", "()Ljava/net/URLConnection;");
        assertEquals(0, count(transform(input, false, false), FTURLConnectionMethodAdapter.HOOK, "wrap"));
        assertEquals(0, count(transform(input, true, true), FTURLConnectionMethodAdapter.HOOK, "wrap"));
    }

    @Test public void exactOwnerNameAndDescriptorAreRequired() {
        for (String[] call : new String[][]{
                {"sample/URL", "openConnection", "()Ljava/net/URLConnection;"},
                {"java/net/URL", "getContent", "()Ljava/lang/Object;"},
                {"java/net/URL", "openConnection", "()Ljava/net/HttpURLConnection;"}}) {
            assertEquals(0, count(transform(fixture(call[0], call[1], call[2]), true, false),
                    FTURLConnectionMethodAdapter.HOOK, "wrap"));
        }
    }

    @Test public void optionParticipatesInGradleCacheKey() throws Exception {
        assertNotNull(FTParameters.class.getMethod("getInstrumentHttpURLConnection").getAnnotation(Input.class));
    }

    @Test public void pluginRejectsAgentWithoutNewHook() {
        FTClassAdapter adapter = new FTClassAdapter(new ClassWriter(0), Opcodes.ASM9,
                Collections.emptyList(), false, Collections.emptyList(), true);
        adapter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "com/ft/sdk/FTSdk", null, "java/lang/Object", null);
        try {
            adapter.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                    "AGENT_VERSION", "Ljava/lang/String;", null, "1.7.6-alpha02");
            fail("Old Agent must fail during build");
        } catch (Error expected) { assertTrue(expected.getMessage().contains(BuildConfig.MIN_SDK_VERSION)); }
        adapter.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                "AGENT_VERSION", "Ljava/lang/String;", null, BuildConfig.MIN_SDK_VERSION);
    }
    @Test public void ignoredMethodAnnotationSurvivesRepeatedTransforms() {
        ClassNode node = new ClassNode();
        new ClassReader(fixture("java/net/URL", "openConnection", "()Ljava/net/URLConnection;")).accept(node, 0);
        node.methods.get(0).visitAnnotation(com.ft.plugin.garble.Constants.IGNORE_ANNOTATION, false).visitEnd();
        ClassWriter writer = new ClassWriter(0); node.accept(writer);
        byte[] first = transform(writer.toByteArray(), true, false);
        assertEquals(0, count(first, FTURLConnectionMethodAdapter.HOOK, "wrap"));
        assertEquals(0, count(transform(first, true, false), FTURLConnectionMethodAdapter.HOOK, "wrap"));
    }

    @Test public void transformedCallsPreserveProxyAndOriginalConnectionAtRuntime() throws Exception {
        for (boolean explicit : new boolean[]{false, true}) {
            for (java.net.Proxy proxy : new java.net.Proxy[]{java.net.Proxy.NO_PROXY,
                    new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress("localhost", 1234))}) {
                final int[] opens = {0};
                java.net.URL url = new java.net.URL(null, "http://example.test/", new java.net.URLStreamHandler() {
                    @Override protected java.net.URLConnection openConnection(java.net.URL u) {
                        assertFalse(explicit);
                        opens[0]++;
                        return new java.net.URLConnection(u) { public void connect() { } };
                    }
                    @Override protected java.net.URLConnection openConnection(java.net.URL u, java.net.Proxy selected) {
                        assertTrue(explicit);
                        assertEquals(proxy, selected);
                        opens[0]++;
                        return new java.net.URLConnection(u) { public void connect() { } };
                    }
                });
                byte[] code = transform(fixture("java/net/URL", "openConnection", explicit
                        ? "(Ljava/net/Proxy;)Ljava/net/URLConnection;" : "()Ljava/net/URLConnection;"), true, false);
                byte[] twice = transform(code, true, false);
                class Loader extends ClassLoader {
                    Class<?> define(byte[] bytes) { return defineClass("sample.Calls", bytes, 0, bytes.length); }
                }
                Class<?> caller = new Loader().define(twice);
                com.ft.sdk.FTURLConnectionInstrumentation.calls = 0;
                java.net.URLConnection result = (java.net.URLConnection) caller.getMethod("call", java.net.URL.class,
                        java.net.Proxy.class).invoke(null, url, proxy);
                assertEquals(url, result.getURL());
                assertEquals(1, opens[0]);
                assertEquals(1, com.ft.sdk.FTURLConnectionInstrumentation.calls);
                assertSame(explicit ? proxy : null, com.ft.sdk.FTURLConnectionInstrumentation.proxy);
            }
        }
    }

}
