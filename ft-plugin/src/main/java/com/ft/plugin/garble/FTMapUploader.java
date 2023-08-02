package com.ft.plugin.garble;

import com.android.build.gradle.AppExtension;
import com.android.utils.FileUtils;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ProjectDependency;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 负责符号文件打包与上传
 */
public class FTMapUploader {

    private final Project project;
    /**
     * debug symbol 路径
     */
    private final static String CMAKE_DEBUG_SYMBOL_PATH = "/intermediates/cmake/debug/obj";
    private final static String NAME_RELEASE_COMPILE_CLASSPATH = "releaseCompileClasspath";

    private final HashMap<String, ObfuscationSettingConfig> obfuscationSettingMap = new HashMap<>();

    /**
     * 数据合并路径
     */
    private final static String SYMBOL_MERGE_PATH_FORMAT = "/tmp/ftSourceMapMerge-%s";
    /**
     * zip 文件打包路径
     */
    private final static String SYMBOL_MERGE_ZIP_PATH_FORMAT = "/tmp/ftSourceMap-%s.zip";
    /**
     * proguard 符号文件
     */
    private final static String PROGUARD_MAPPING_PATH = "/outputs/proguard/%s/mapping/mapping.txt";

    private final HashMap<String, ArrayList<String>> symbolPathsMap = new HashMap<>();
    private final String tmpBuildPathFormat;
    private final String zipBuildPathFormat;
    private final String proguardBuildPathFormat;
    private final FTExtension extension;
    private final HashMap<String, ProductFlavorModel> flavorModelHashMap = new HashMap<>();


    public FTMapUploader(Project project, FTExtension extension) {
        this.project = project;
        String buildPath = project.getBuildDir().getAbsolutePath();
        this.tmpBuildPathFormat = buildPath + SYMBOL_MERGE_PATH_FORMAT;
        this.zipBuildPathFormat = buildPath + SYMBOL_MERGE_ZIP_PATH_FORMAT;
        this.proguardBuildPathFormat = buildPath + PROGUARD_MAPPING_PATH;
        this.extension = extension;
        extension.getOther().forEach(valueModel -> flavorModelHashMap.put(valueModel.getName(), valueModel));
    }

    /**
     * 上传混淆符号文件
     */
    public void configMapUpload() {
        if (flavorModelHashMap.isEmpty()) {
            if (!extension.autoUploadMap) {
                return;
            }
        }
        AppExtension appExtension = (AppExtension) project.getProperties().get("android");

        appExtension.getApplicationVariants().all(applicationVariant -> {
            String variantName = applicationVariant.getName();

            String capVariantName = FTStringUtils.captitalizedString(variantName);

            String assembleTaskName = "assemble" + capVariantName;

            Task ftTask = project.getTasks().create("ft" + capVariantName + "UploadSymbolMap", task -> {

            }).doLast(task -> {
                ProductFlavorModel model = getFlavorModelFromName(variantName);
                Logger.debug("ProductFlavorModel:" + model);

                if (!model.isAutoUploadMap() && !model.isAutoUploadNativeDebugSymbol()) {
                    return;
                }

                String tmpBuildPath = String.format(tmpBuildPathFormat, variantName);
                String zipBuildPath = String.format(zipBuildPathFormat, variantName);
                try {
                    //删除之前的 cache
                    FileUtils.deleteIfExists(new File(tmpBuildPath));
                    FileUtils.deleteIfExists(new File(zipBuildPath));
                } catch (IOException e) {
                    Logger.debug("tmp  delete error:" + e.getMessage());
                }
                try {
                    ObfuscationSettingConfig config = obfuscationSettingMap.get(assembleTaskName);
                    Logger.debug("task:" + assembleTaskName + ",config:" + config + "");
                    if (config != null) {
                        if (model.isAutoUploadNativeDebugSymbol()) {
                            ArrayList<String> symbolPaths = symbolPathsMap.get(model.getName());
                            if (symbolPaths != null && !symbolPaths.isEmpty()) {
                                FTFileUtils.copyDifferentFolderFilesIntoOne(tmpBuildPath, symbolPaths.toArray(new String[0]));
                            }
                        }
                        if (model.isAutoUploadMap()) {
                            if (new File(config.mappingOutputPath).exists()) {
                                FTFileUtils.copyFile(new File(config.mappingOutputPath), new File(tmpBuildPath + "/mapping.txt"));
                            }
                        }
                        FTFileUtils.zipFiles(new File(tmpBuildPath).listFiles(), new File(zipBuildPath));
                        uploadWithParams(config, model, zipBuildPath);
                    }

                } catch (IOException | InterruptedException e) {
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
                    if (applicationVariant.getBuildType().isMinifyEnabled()) {

                        applicationVariant.getMappingFileProvider().get().getFiles().forEach(file -> {
                            if (file != null && file.exists()) {
                                ObfuscationSettingConfig config = new ObfuscationSettingConfig();
                                config.applicationId = applicationVariant.getApplicationId();
                                config.versionName = applicationVariant.getVersionName();
                                config.versionCode = applicationVariant.getVersionCode();
                                config.mappingOutputPath = file.getAbsoluteFile().toString();

                                Logger.debug("Map Config:" + config + ",task:" + task.getName());

                                obfuscationSettingMap.put(task.getName(), config);
                            }
                        });

                    } else {
                        boolean isProguardSet = false;
                        try {
                            project.getExtensions().getByName("proguard");
                            isProguardSet = true;
                        } catch (UnknownDomainObjectException e) {
                            Logger.error(e.getMessage());
                        }

                        if (isProguardSet) {
                            ObfuscationSettingConfig config = new ObfuscationSettingConfig();
                            config.applicationId = applicationVariant.getApplicationId();
                            config.versionName = applicationVariant.getVersionName();
                            config.versionCode = applicationVariant.getVersionCode();
                            config.mappingOutputPath = String.format(proguardBuildPathFormat,
                                    variantName);
                            Logger.debug("Map Config:" + config + ",task:" + task.getName());
                            obfuscationSettingMap.put(task.getName(), config);
                        }
                    }
                }
            });
        });

    }


    /**
     * 上传 debug native symbol 文件
     */
    public void configNativeSymbolUpload() {
        if (flavorModelHashMap.isEmpty()) {
            if (!extension.autoUploadNativeDebugSymbol) {
                return;
            }
        }
        project.afterEvaluate(p -> {
            AppExtension appExtension = (AppExtension) p.getProperties().get("android");

            if (appExtension.getProductFlavors().size() > 0) {
                appExtension.getProductFlavors().all(flavor -> {
                    // 获取 Flavor 的名称
                    String flavorName = flavor.getName();
                    ArrayList<String> symbolPaths = symbolPathsMap.computeIfAbsent(flavorName, k -> new ArrayList<>());
                    appendSymbolPath(p, symbolPaths, flavorName);

                });
            } else {
                ArrayList<String> symbolPaths = symbolPathsMap.computeIfAbsent("release", k -> new ArrayList<>());
                appendSymbolPath(p, symbolPaths, "");
            }


        });

    }

    /**
     * 扫描并添加 native cmake build 路径
     *
     * @param p
     * @param list
     * @param flavor
     */
    private void appendSymbolPath(Project p, ArrayList<String> list, String flavor) {

        p.getAllprojects().forEach(subProject -> {
            String debugSymbolPath = subProject.getBuildDir().getAbsolutePath() + CMAKE_DEBUG_SYMBOL_PATH;

            File file = new File(debugSymbolPath);
            if (file.exists()) {
                list.add(debugSymbolPath);
            }
        });
        String name = flavor.length() == 0 ? NAME_RELEASE_COMPILE_CLASSPATH :
                (flavor + FTStringUtils.captitalizedString(NAME_RELEASE_COMPILE_CLASSPATH));
        Configuration configuration = p.getConfigurations().findByName(name);
        if (configuration != null) {
            String rootPath = p.getRootDir().getAbsolutePath();
            configuration.getAllDependencies().forEach(dependency -> {
                if (dependency instanceof ProjectDependency) {
                    String moduleName = dependency.getName();
                    String debugSymbolPath = rootPath + "/" + moduleName + "/build" + CMAKE_DEBUG_SYMBOL_PATH;
                    File file = new File(debugSymbolPath);
                    Logger.debug("debugSymbolPath:" + debugSymbolPath);
                    if (file.exists()) {
                        list.add(debugSymbolPath);
                    }
                }
            });
            if (symbolPathsMap.isEmpty()) {
                Logger.error("native symbol not found");
            } else {
                Logger.debug("paths:" + symbolPathsMap);
            }

        }

    }

    /**
     * 上传符号文件
     *
     * @param settingConfig
     * @throws IOException
     * @throws InterruptedException
     */
    private void uploadWithParams(ObfuscationSettingConfig settingConfig, ProductFlavorModel model, String zipBuildPath) throws IOException, InterruptedException {
        Logger.debug(model.toString());
        String cmd = "curl -X POST " + model.getDatakitDCAUrl() + "/v1/rum/sourcemap?app_id="
                + model.getAppId() + "&env=" + model.getEnv() + "&version="
                + settingConfig.versionName + "&platform=android -F file=@" + zipBuildPath + " -H Content-Type:multipart/form-data";

        Logger.debug(cmd);
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

        Logger.debug("response:" + sb);

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            Logger.error("map 文件上传失败");
            Logger.error("exit code::" + exitCode);
        }

        process.destroy();
    }

    private ProductFlavorModel getFlavorModelFromName(String variantName) {
        ProductFlavorModel model = flavorModelHashMap
                .get(variantName.replace("Release", ""));
        if (model != null) {
            //多个 flavor
            model.mergeFTExtension(extension);
            return model;
        } else {
            //不设置 flavor, 此处 variantName = release
            model = new ProductFlavorModel(variantName);
            model.setFromFTExtension(extension);
        }
        return model;
    }


    static class ObfuscationSettingConfig {
        /**
         * 包名
         */
        String applicationId;
        /**
         * 版本号 字符 例如 1.0.0
         */
        String versionName;
        /**
         * Build Code
         */
        int versionCode;
        /**
         * map 输出地址
         */
        String mappingOutputPath;

        @Override
        public String toString() {
            return "SettingConfig{" +
                    "applicationId='" + applicationId + '\'' +
                    ", versionName='" + versionName + '\'' +
                    ", versionCode=" + versionCode +
                    ", outMappingOutputPath='" + mappingOutputPath + '\'' +
                    '}';
        }
    }

}
