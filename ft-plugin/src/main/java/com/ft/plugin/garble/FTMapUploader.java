package com.ft.plugin.garble;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ProjectDependency;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


public class FTMapUploader {

    private final Project project;
    private final static String CMAKE_DEBUG_SYMBOL_PATH = "/intermediates/cmake/debug/obj";

    private final HashMap<String, ProguardSettingConfig> proguardSettingMap = new HashMap<>();

    private final static String SYMBOL_MERGE_PATH = "/tmp/ft-mergeNativeSymbols";
    private final static String SYMBOL_MERGE_ZIP_PATH = "/tmp/symbol.zip";

    private final ArrayList<String> symbolPaths = new ArrayList<>();
    private final String tmpBuildPath;
    private final String zipBuildPath;


    public FTMapUploader(Project project) {
        this.project = project;
        String buildPath = project.getBuildDir().getAbsolutePath();
        tmpBuildPath = buildPath + SYMBOL_MERGE_PATH;
        zipBuildPath = buildPath + SYMBOL_MERGE_ZIP_PATH;
    }

    /**
     * 上传 proguard 符号文件
     */
    public void configProguardUpload() {
        AppExtension appExtension = (AppExtension) project.getProperties().get("android");

        appExtension.getApplicationVariants().all(applicationVariant -> {
            String variantName = applicationVariant.getName();

            String capVariantName = variantName.substring(0, 1).toUpperCase() + variantName.substring(1);

            String assembleTaskName = "assemble" + capVariantName;

            Task ftTask = project.getTasks().create("ft" + variantName + "UploadSymbolMap", task -> {

            }).doLast(task -> {

                try {
                    ProguardSettingConfig config = proguardSettingMap.get(assembleTaskName);
                    Logger.debug("task:" + assembleTaskName + ",config:" + config + "");
                    if (config != null) {

                        if (!symbolPaths.isEmpty()) {
                            mergeFileAndZip(tmpBuildPath, symbolPaths.toArray(new String[0]));
                        }
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
                            ProguardSettingConfig config = new ProguardSettingConfig();
                            config.applicationId = applicationVariant.getApplicationId();
                            config.versionName = applicationVariant.getVersionName();
                            config.versionCode = applicationVariant.getVersionCode();
                            config.mappingOutputPath = file.getAbsoluteFile().toString();

                            Logger.debug("Map Config:" + config + ",task:" + task.getName());

                            proguardSettingMap.put(task.getName(), config);
                        }
                    });
                }
            });
        });

    }

    /**
     * 合并并生成 zip
     * @param tmpBuildPath
     * @param symbolPaths
     * @throws IOException
     */
    private void mergeFileAndZip(String tmpBuildPath, String... symbolPaths) throws IOException {
        FTFileUtils.copyDifferentFolderFilesIntoOne(tmpBuildPath, symbolPaths);
        FTFileUtils.zipDirectory(new File(tmpBuildPath), new File(zipBuildPath));
    }


    /**
     * 上传 debug native symbol 文件
     */
    public void configNativeSymbolUpload() {
        project.afterEvaluate(p -> {

            p.getAllprojects().forEach(subProject -> {
                String debugSymbolPath = subProject.getBuildDir().getAbsolutePath() + CMAKE_DEBUG_SYMBOL_PATH;

                File file = new File(debugSymbolPath);
                if (file.exists()) {
                    symbolPaths.add(debugSymbolPath);
                }

            });

            Configuration configuration = project.getConfigurations().findByName("releaseCompileClasspath");
            if (configuration != null) {
                String rootPath = p.getRootDir().getAbsolutePath();
                configuration.getAllDependencies().forEach(dependency -> {
                    if (dependency instanceof ProjectDependency) {
                        String moduleName = dependency.getName();
                        String debugSymbolPath = rootPath + "/" + moduleName + CMAKE_DEBUG_SYMBOL_PATH;
                        File file = new File(debugSymbolPath);
                        if (file.exists()) {
                            symbolPaths.add(debugSymbolPath);
                        }
                    }
                });
                if (symbolPaths.isEmpty()) {
                    Logger.error("native symbol not found");
                } else {
                    Logger.debug("paths:" + symbolPaths.toString());
                }

            }

        });

    }

    /**
     *  上传符号文件
     * @param settingConfig
     * @throws IOException
     * @throws InterruptedException
     */
    private void uploadWithParams(ProguardSettingConfig settingConfig) throws IOException, InterruptedException {
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


    static class ProguardSettingConfig {
        String applicationId;
        String versionName;
        int versionCode;
        String mappingOutputPath;
        String debugSymbolPath;

        @Override
        public String toString() {
            return "SettingConfig{" +
                    "applicationId='" + applicationId + '\'' +
                    ", versionName='" + versionName + '\'' +
                    ", versionCode=" + versionCode +
                    ", debugSymbolPath=" + debugSymbolPath +
                    ", outMappingOutputPath='" + mappingOutputPath + '\'' +
                    '}';
        }
    }

}
