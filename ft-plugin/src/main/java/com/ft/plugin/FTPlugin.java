package com.ft.plugin;

import com.android.build.gradle.AppExtension;
import com.ft.plugin.garble.FTExtension;
import com.ft.plugin.garble.FTMapUploader;
import com.ft.plugin.garble.Logger;
import com.ft.plugin.garble.asm.FTTransform;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Collections;

/**
 * BY huangDianHua
 * DATE:2019-11-29 12:33
 * Description:插件入口
 */
public class FTPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        AppExtension appExtension = (AppExtension) project.getProperties().get("android");
        project.getExtensions().create("FTExt", FTExtension.class);
        appExtension.registerTransform(new FTTransform(project), Collections.EMPTY_LIST);

        project.afterEvaluate(p -> {

            FTExtension extension = (FTExtension) p.getExtensions().getByName("FTExt");

            Logger.setDebug(extension.showLog);

            FTMapUploader f = new FTMapUploader(p);


            if (extension.autoUploadProguardMap) {
                f.configProguardUpload();
            }

            if (extension.autoUploadNativeDebugSymbol) {
                f.configNativeSymbolUpload();
            }


        });

    }
}
