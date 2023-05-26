package com.ft.plugin.garble.asm;

import com.android.build.api.instrumentation.AsmClassVisitorFactory;
import com.android.build.api.instrumentation.ClassContext;
import com.android.build.api.instrumentation.ClassData;
import com.android.build.api.instrumentation.InstrumentationParameters;
import com.ft.plugin.garble.bytecode.FTClassAdapter;

import org.objectweb.asm.ClassVisitor;

/**
 * 创建 asm class visitor
 *
 * @author Brandon
 */
public abstract class FTTransform implements AsmClassVisitorFactory<InstrumentationParameters.None> {

    @Override
    public ClassVisitor createClassVisitor(ClassContext classContext, ClassVisitor nextClassVisitor) {
        return new FTClassAdapter(nextClassVisitor);
    }

    @Override
    public boolean isInstrumentable(ClassData classData) {
        return true;
    }
}
