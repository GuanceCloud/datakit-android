package com.ft.plugin;

import com.android.build.api.instrumentation.FramesComputationMode;
import com.android.build.api.instrumentation.InstrumentationScope;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.Variant;
import com.ft.plugin.garble.FTExtension;
import com.ft.plugin.garble.FTMapUploader;
import com.ft.plugin.garble.Logger;
import com.ft.plugin.garble.asm.FTParameters;
import com.ft.plugin.garble.asm.FTTransform;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;



/*
  app
  |--build.gradle

    FTExt {
        showLog = true

        autoUploadMap = false
        autoUploadNativeDebugSymbol = false
        datakitUrl = ft_env.datakitRUMUrl
        datawayToken = ft_env.datawayToken
        appId = ft_env.rumAppid
        env = 'common'

        prodFlavors {
            prodTest {
                autoUploadMap = true
                autoUploadNativeDebugSymbol = true
                datakitUrl = ft_env.datakitRUMUrl
                datawayToken = ft_env.datawayToken
                appId = ft_env.rumAppid
                env = ft_env.prodTestEnv
            }
            prodPublish {
                autoUploadMap = true
                autoUploadNativeDebugSymbol = true
                datakitUrl = ft_env.datakitRUMUrl
                datawayToken = ft_env.datawayToken
                appId = ft_env.rumAppid
                env = ft_env.prodPublishEnv
            }
        }
    }
 */

/**
 * BY huangDianHua
 * DATE:2019-11-29 12:33
 * Description: Plugin entry point
 */
public class FTPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        FTExtension extension = project.getExtensions().create("FTExt", FTExtension.class, project);

        project.afterEvaluate(p -> {
            //Pass parameter object
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
                    ",ASM Version:" + extension.asmVersion);

            FTMapUploader f = new FTMapUploader(p, extension);
            f.configMapUpload();
        });

        AndroidComponentsExtension androidComponents = project.getExtensions().getByType(AndroidComponentsExtension.class);
        androidComponents.onVariants(androidComponents.selector().all(), new Action<Variant>() {
            @Override
            public void execute(Variant variant) {
                variant.getInstrumentation().transformClassesWith(
                        FTTransform.class,
                        InstrumentationScope.ALL,
                        new Function1<FTParameters, Unit>() {
                            @Override
                            public Unit invoke(FTParameters parameters) {
                                // Now extension is already user-configured content
                                parameters.getIgnorePackages().set(extension.ignorePackages);
                                parameters.getKnownWebViewClasses().set(extension.knownWebViewClasses);
                                parameters.getAsmVersion().set(extension.asmVersion);
                                parameters.getVerboseLog().set(extension.verboseLog);
                                return Unit.INSTANCE;
                            }
                        }
                );

                variant.getInstrumentation()
                        .setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS);

            }
        });


    }
}
