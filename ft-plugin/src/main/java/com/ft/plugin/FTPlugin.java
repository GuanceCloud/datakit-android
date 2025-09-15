package com.ft.plugin;

import com.android.build.gradle.AppExtension;
import com.ft.plugin.garble.FTExtension;
import com.ft.plugin.garble.FTMapUploader;
import com.ft.plugin.garble.Logger;
import com.ft.plugin.garble.asm.FTTransform;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.util.Collections;



/*
  app
  |--build.gradle

  FTExt {                                        //Parameter object
      showLog = true                             //Whether to show logs

      autoUploadMap = false                     //Whether to upload map
      autoUploadNativeDebugSymbol = false       //Whether to upload c/c++ native debug symbol files
      datakitDCAUrl = ft_env.datakitDCAUrl      // datakit DCA address
      appId = ft_env.rumAppid                   // appid
     env = 'common'                            //Corresponding environment

       //Flavor override logic
      prodFlavors {
          prodTest {
              autoUploadMap = true
              autoUploadNativeDebugSymbol = true
              datakitDCAUrl = ft_env.datakitDCAUrl
              appId = ft_env.rumAppid
              env = ft_env.prodTestEnv
          }
          prodPublish {
              autoUploadMap = true
              autoUploadNativeDebugSymbol = true
              datakitDCAUrl = ft_env.datakitDCAUrl
              appId = ft_env.rumAppid
              env = ft_env.prodPublishEnv
          }
      }
  }
 */

/**
 * BY huangDianHua
 * DATE:2019-11-29 12:33
 * Description:Plugin entry point
 */
public class FTPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        AppExtension appExtension = (AppExtension) project.getProperties().get("android");
        project.getExtensions().create("FTExt", FTExtension.class, project);
        // Register Transform immediately, not in afterEvaluate
        appExtension.registerTransform(new FTTransform(project), Collections.EMPTY_LIST);
        project.afterEvaluate(p -> {
            //Parameter object
            FTExtension extension = (FTExtension) p.getExtensions().getByName("FTExt");

            // Initialize logger based on extension configuration
            if (extension.enableFileLog) {
                String logFile;
                if (extension.logFilePath != null && !extension.logFilePath.isEmpty()) {
                    logFile = extension.logFilePath;
                } else {
                    logFile = p.getBuildDir().getAbsolutePath() + File.separator + "ft-plugin.log";
                }
                Logger.init(logFile);
            }

            Logger.setDebug(extension.showLog);
            Logger.debug("Plugin Version:" + BuildConfig.PLUGIN_VERSION +
                    ",ASM Version:asm7");

            FTMapUploader f = new FTMapUploader(p, extension);
            f.configMapUpload();
        });
    }
}
