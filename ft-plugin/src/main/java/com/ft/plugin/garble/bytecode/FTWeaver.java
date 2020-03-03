package com.ft.plugin.garble.bytecode;

import com.ft.plugin.garble.ClassNameAnalytics;
import com.ft.plugin.garble.FTExtension;
import com.ft.plugin.garble.FTTransformHelper;
import com.ft.plugin.garble.asm.BaseWeaver;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * From https://github.com/Leaking/Hunter/blob/master/hunter-timing-plugin/src/main/groovy/com/quinn/hunter/plugin/timing/bytecode/TimingWeaver.java
 * DATE:2019-11-29 15:16
 * Description:
 */
public final class FTWeaver extends BaseWeaver {
    private FTExtension ftExtension;
    private FTTransformHelper ftTransformHelper;

    @Override
    public void setExtension(Object extension) {
        if(extension == null) return;
        this.ftExtension = (FTExtension) extension;
    }

    @Override
    public void setFTTransformHelper(FTTransformHelper ftTransformHelper){
        this.ftTransformHelper = ftTransformHelper;
    }

    @Override
    public boolean isWeavableClass(String fullQualifiedClassName) {
        boolean superResult = super.isWeavableClass(fullQualifiedClassName);
        boolean isByteCodePlugin = ClassNameAnalytics.isFTSDKFile(fullQualifiedClassName);
        return superResult && !isByteCodePlugin;
    }

    @Override
    protected ClassVisitor wrapClassWriter(ClassWriter classWriter) {
        return new FTClassAdapter(classWriter,ftTransformHelper);
    }

}

