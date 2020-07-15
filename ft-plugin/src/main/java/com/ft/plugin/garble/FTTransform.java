package com.ft.plugin.garble;

import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.ft.plugin.garble.asm.BaseTransform;
import com.ft.plugin.garble.bytecode.FTWeaver;

import org.gradle.api.Project;

import java.io.IOException;

/**
 * BY huangDianHua
 * DATE:2019-11-29 13:40
 * Description:FT字节码转换类
 */
public class FTTransform extends BaseTransform {
    private Project project;
    private FTExtension ftExtension;
    private FTTransformHelper ftTransformHelper;

    public FTTransform(Project project) {
        super(project);
        this.project = project;
        project.getExtensions().create("FTExt", FTExtension.class);
        this.bytecodeWeaver = new FTWeaver();
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        ftExtension = (FTExtension) project.getExtensions().getByName("FTExt");
        Logger.setDebug(ftExtension.showLog);
        ftTransformHelper = new FTTransformHelper(ftExtension);
        bytecodeWeaver.setExtension(ftExtension);
        bytecodeWeaver.setFTTransformHelper(ftTransformHelper);
        super.transform(transformInvocation);
    }
}
