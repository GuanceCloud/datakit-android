package com.ft.plugin;

import com.android.build.gradle.AppExtension;
import com.ft.plugin.garble.FTExtension;
import com.ft.plugin.garble.FTMapUploader;
import com.ft.plugin.garble.Logger;
import com.ft.plugin.garble.PluginConfigManager;
import com.ft.plugin.garble.asm.FTTransform;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Collections;



/*
  app
  |--build.gradle

  FTExt {                                        //传参对象
      showLog = true                             //是否显示日志

      autoUploadMap = false                     //是否上传 map
      autoUploadNativeDebugSymbol = false       //是否上传 c/c++ native debug symbol 文件
      datakitDCAUrl = ft_env.datakitDCAUrl      // datakit DCA 地址
      appId = ft_env.rumAppid                   // appid
     env = 'common'                            //对应环境

       //Flavor 覆盖逻辑
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
 * Description:插件入口
 */
public class FTPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        AppExtension appExtension = (AppExtension) project.getProperties().get("android");
        project.getExtensions().create("FTExt", FTExtension.class, project);
        appExtension.registerTransform(new FTTransform(project), Collections.EMPTY_LIST);

        project.afterEvaluate(p -> {
            //传参数对象
            FTExtension extension = (FTExtension) p.getExtensions().getByName("FTExt");
            PluginConfigManager.get().setExtension(extension);

            Logger.setDebug(extension.showLog);
            Logger.debug("ASM Version:" + extension.asmVersion);

            FTMapUploader f = new FTMapUploader(p, extension);
            f.configMapUpload();
            f.configNativeSymbolUpload();
        });

    }
}
