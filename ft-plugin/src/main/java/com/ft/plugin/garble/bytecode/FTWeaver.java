package com.ft.plugin.garble.bytecode;

import com.ft.plugin.garble.ClassNameAnalytics;
import com.ft.plugin.garble.FTExtension;
import com.ft.plugin.garble.asm.BaseWeaver;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * <a href="https://github.com/Leaking/Hunter/blob/master/hunter-timing-plugin/src/main/groovy/com/quinn/hunter/plugin/timing/bytecode/TimingWeaver.java">Reference material</a>
 * DATE:2019-11-29 15:16
 * Description:
 */
public final class FTWeaver extends BaseWeaver {
    private final FTExtension extension;

    public FTWeaver(FTExtension extension) {
        this.extension = extension;
    }

    @Override
    public boolean isWeavableClass(String fullQualifiedClassName) {
        boolean superResult = super.isWeavableClass(fullQualifiedClassName);
        boolean isByteCodePlugin = ClassNameAnalytics.isFTSDKFile(fullQualifiedClassName);
        return superResult && !isByteCodePlugin;
    }

    @Override
    protected ClassVisitor wrapClassWriter(ClassWriter classWriter) {
        return new FTClassAdapter(classWriter, extension.ignorePackages);
    }

}

