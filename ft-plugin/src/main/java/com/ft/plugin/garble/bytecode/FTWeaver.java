package com.ft.plugin.garble.bytecode;

import com.ft.plugin.garble.ClassNameAnalytics;
import com.ft.plugin.garble.FTExtension;
import com.ft.plugin.garble.FTTransformHelper;
import com.ft.plugin.garble.asm.BaseWeaver;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * BY huangDianHua
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
        if(ftExtension != null) {
            //whitelist is prior to to blacklist
            if(!ftExtension.whitelist.isEmpty()) {
                boolean inWhiteList = false;
                for(String item : ftExtension.whitelist) {
                    if(fullQualifiedClassName.startsWith(item)) {
                        inWhiteList = true;
                    }
                }
                return superResult && !isByteCodePlugin && inWhiteList;
            }
            if(!ftExtension.blacklist.isEmpty()) {
                boolean inBlackList = false;
                for(String item : ftExtension.blacklist) {
                    if(fullQualifiedClassName.startsWith(item)) {
                        inBlackList = true;
                    }
                }
                return superResult && !isByteCodePlugin && !inBlackList;
            }
        }
        return superResult && !isByteCodePlugin;
    }

    @Override
    protected ClassVisitor wrapClassWriter(ClassWriter classWriter) {
        return new FTClassAdapter(classWriter,ftTransformHelper);
    }

}

