package com.ft.plugin;

import com.android.build.api.instrumentation.FramesComputationMode;
import com.android.build.api.instrumentation.InstrumentationParameters;
import com.android.build.api.instrumentation.InstrumentationScope;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.Variant;
import com.ft.plugin.garble.FTExtension;
import com.ft.plugin.garble.FTMapUploader;
import com.ft.plugin.garble.Logger;
import com.ft.plugin.garble.asm.FTTransform;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;



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
        project.getExtensions().create("FTExt", FTExtension.class, project);

        project.afterEvaluate(p -> {
            //传参数对象
            FTExtension extension = (FTExtension) p.getExtensions().getByName("FTExt");

            Logger.setDebug(extension.showLog);

            FTMapUploader f = new FTMapUploader(p, extension);
            f.configMapUpload();
            f.configNativeSymbolUpload();
        });

        AndroidComponentsExtension extension = project.getExtensions().getByType(AndroidComponentsExtension.class);
        extension.onVariants(extension.selector().all(), new Action<Variant>() {
            @Override
            public void execute(Variant variant) {
                variant.getInstrumentation()
                        .transformClassesWith(FTTransform.class, InstrumentationScope.ALL, new Function1<InstrumentationParameters.None, Unit>() {
                            @Override
                            public Unit invoke(InstrumentationParameters.None none) {

                                return Unit.INSTANCE;
                            }
                        });
                variant.getInstrumentation()
                        .setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS);

            }
        });


    }
}
