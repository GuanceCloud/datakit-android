package com.ft.plugin.garble;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;


public class FTMapUploader {

    private final Project project;

    private final HashMap<String, SettingConfig> settingMap = new HashMap<>();


    public FTMapUploader(Project project) {
        this.project = project;

    }

    public void configUpload() {
        AppExtension appExtension = (AppExtension) project.getProperties().get("android");

        appExtension.getApplicationVariants().all(applicationVariant -> {
            String variantName = applicationVariant.getName();

            String capVariantName = variantName.substring(0, 1).toUpperCase() + variantName.substring(1);

            String assembleTaskName = "assemble" + capVariantName;

            Task ftTask = project.getTasks().create("ft" + variantName + "UploadSymbolMap", task -> {

            }).doLast(task -> {

                try {
                    SettingConfig config = settingMap.get(assembleTaskName);
                    Logger.debug("task:" + assembleTaskName + ",config:" + config + "");
                    if (config != null) {
                        uploadWithParams(config);
                    }

                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

            });

            if (!applicationVariant.getName().endsWith("Debug")) {
                project.afterEvaluate(p -> {
                    p.getTasks().getAt(assembleTaskName).finalizedBy(ftTask);
                });
            }

            applicationVariant.getAssembleProvider().get().doLast(task -> {

                if (!task.getName().endsWith("Debug")) {
                    applicationVariant.getMappingFileProvider().get().getFiles().forEach(file -> {
                        if (file != null && file.exists()) {
                            SettingConfig config = new SettingConfig();
                            config.applicationId = applicationVariant.getApplicationId();
                            config.versionName = applicationVariant.getVersionName();
                            config.versionCode = applicationVariant.getVersionCode();
                            config.outMappingOutputPath = file.getAbsoluteFile().toString();

                            Logger.debug("Map Config:" + config + ",task:" + task.getName());

                            settingMap.put(task.getName(), config);


                        }
                    });
                }
            });
        });

    }

    private void uploadWithParams(SettingConfig settingConfig) throws IOException, InterruptedException {
        String cmd = "curl -v http://www.baidu.com --fail";
        ProcessBuilder builder = new ProcessBuilder(cmd.split(" "));
        builder.redirectErrorStream(true);

        Process process = builder.start();
        InputStream ins = process.getInputStream();
        BufferedReader read = new BufferedReader(new InputStreamReader(ins));

        StringBuilder sb = new StringBuilder();
        read.lines().forEach(s -> {
            sb.append(s).append("\n");

        });

        process.waitFor();

        Logger.debug("response:" + sb.toString());

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            Logger.error("map 文件上传失败");
            Logger.error("exit code::" + exitCode);
        }

        process.destroy();
    }


    static class SettingConfig {
        String applicationId;
        String versionName;
        int versionCode;
        boolean minifyEnable;
        String outMappingOutputPath;

        @Override
        public String toString() {
            return "SettingConfig{" +
                    "applicationId='" + applicationId + '\'' +
                    ", versionName='" + versionName + '\'' +
                    ", versionCode=" + versionCode +
                    ", minifyEnable=" + minifyEnable +
                    ", outMappingOutputPath='" + outMappingOutputPath + '\'' +
                    '}';
        }
    }

}
