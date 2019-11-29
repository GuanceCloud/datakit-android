package com.ft.plugin.garble;

import com.android.build.api.transform.Context;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformOutputProvider;
import com.ft.plugin.garble.asm.BaseTransform;
import com.ft.plugin.garble.asm.RunVariant;
import com.ft.plugin.garble.bytecode.FTWeaver;

import org.gradle.api.Project;

import java.io.IOException;
import java.util.Collection;

/**
 * BY huangDianHua
 * DATE:2019-11-29 13:40
 * Description:FT字节码转换类
 */
public class FTTransform extends BaseTransform {
    private Project project;
    private FTExtension ftExtension;

    public FTTransform(Project project) {
        super(project);
        this.project = project;
        project.getExtensions().create("FTExt", FTExtension.class);
        this.bytecodeWeaver = new FTWeaver();
    }

    @Override
    public void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        ftExtension = (FTExtension) project.getExtensions().getByName("FTExt");
        bytecodeWeaver.setExtension(ftExtension);
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental);
    }

    protected RunVariant getRunVariant() {
        return ftExtension.runVariant;
    }

    @Override
    protected boolean inDuplcatedClassSafeMode() {
        return ftExtension.duplcatedClassSafeMode;
    }
}
