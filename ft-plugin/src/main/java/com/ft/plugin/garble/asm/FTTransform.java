package com.ft.plugin.garble.asm;

import com.android.build.api.instrumentation.AsmClassVisitorFactory;
import com.android.build.api.instrumentation.ClassContext;
import com.android.build.api.instrumentation.ClassData;
import com.ft.plugin.garble.bytecode.FTClassAdapter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * 创建 asm class visitor
 * <p>
 * 仅支持 AGP 7.4.2 以上，Gradle 7.2.0 以上，其他版本查看分支 plugin_legacy_support
 *
 * @author Brandon
 */
public abstract class FTTransform implements AsmClassVisitorFactory<FTParameters> {

    @Override
    public boolean isInstrumentable(ClassData classData) {
        return true;
    }

    @Override
    public ClassVisitor createClassVisitor(ClassContext classContext, ClassVisitor classVisitor) {
        List<String> ignorePackages = getParameters().get().getIgnorePackages().get();

        String asmVersion = getParameters().get().getAsmVersion().get();
        int asm = Opcodes.ASM9;
        if ("asm8".equalsIgnoreCase(asmVersion)) {
            asm = Opcodes.ASM8;
        } else if ("asm7".equalsIgnoreCase(asmVersion)) {
            asm = Opcodes.ASM7;
        }
        return new FTClassAdapter(classVisitor, asm, ignorePackages);
    }
}
