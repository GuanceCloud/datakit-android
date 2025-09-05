package com.ft.plugin.garble;

import com.android.build.gradle.AppExtension;

import org.apache.tools.ant.util.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownDomainObjectException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Responsible for symbol file packaging and upload
 */
public class FTMapUploader {

    private final Project project;
    /**
     * so build symbol generation path
     */
    private final static String MERGED_LIB_PATH = "build/intermediates/merged_native_libs";

    /**
     * Unity Symbol generation path
     */
    private final static String UNITY_SYMBOLS_PATH = "/unityLibrary/symbols";

    /**
     * R8 obfuscation generation path
     */
    private final HashMap<String, ObfuscationSettingConfig> obfuscationSettingMap = new HashMap<>();

    /**
     * Data merge path
     */
    private final static String SYMBOL_MERGE_PATH_FORMAT = "/tmp/ftSourceMapMerge-%s";
    /**
     * zip file packaging path
     */
    private final static String SYMBOL_MERGE_ZIP_PATH_FORMAT = "/tmp/ftSourceMap-%s.zip";
    /**
     * proguard symbol file
     */
    private final static String PROGUARD_MAPPING_PATH = "/outputs/proguard/%s/mapping/mapping.txt";


    /**
     * zip packaging object, temporary generation path
     */
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
     * Upload obfuscation symbol file and native debug symbol file
     */
    public void configMapUpload() {
        if (flavorModelHashMap.isEmpty()) {
            if (!extension.autoUploadMap && !extension.autoUploadNativeDebugSymbol) {
                return;
            }
        }
        AppExtension appExtension = (AppExtension) project.getProperties().get("android");

        appExtension.getApplicationVariants().all(applicationVariant -> {
            String variantName = applicationVariant.getName();

            String capVariantName = FTStringUtils.captitalizedString(variantName);

            String assembleTaskName = "assemble" + capVariantName;
            Task zipTask = project.getTasks().create("ft" + capVariantName + "ZipSourceMap", task -> {
                    })
                    .doLast(task -> {
                        ProductFlavorModel model = getFlavorModelFromName(variantName);
                        Logger.debug("ZipSourceMap:" + model);

                        if (!model.isAutoUploadMap() && !model.isAutoUploadNativeDebugSymbol()) {
                            return;
                        }

                        ArrayList<String> symbolPaths = new ArrayList<>();
                        if (model.isAutoUploadNativeDebugSymbol()) {
                            appendSymbolPath(project, symbolPaths, variantName, model.getNativeLibPath());
                        }

                        String tmpBuildPath = String.format(tmpBuildPathFormat, variantName);
                        String zipBuildPath = String.format(zipBuildPathFormat, variantName);
                        model.setZipPath(zipBuildPath);

                        // Delete previous cache
                        try {
                            deleteRecursively(new File(tmpBuildPath));
                            FileUtils.delete(new File(zipBuildPath));

                            ObfuscationSettingConfig config = obfuscationSettingMap.get(assembleTaskName);
                            Logger.debug("task:" + assembleTaskName + ",config:" + config);
                            if (config != null) {
                                if (model.isAutoUploadNativeDebugSymbol()) {
                                    if (!symbolPaths.isEmpty()) {
                                        FTFileUtils.copyDifferentFolderFilesIntoOne(tmpBuildPath, symbolPaths.toArray(new String[0]));
                                    } else {
                                        Logger.error("not find native symbol path");
                                    }
                                }
                                if (model.isAutoUploadMap()) {
                                    if (!config.mappingOutputPath.isEmpty() && new File(config.mappingOutputPath).exists()) {
                                        FTFileUtils.copyFile(new File(config.mappingOutputPath), new File(tmpBuildPath + "/mapping.txt"));
                                    } else {
                                        Logger.error("Mapping path empty or File not found");
                                    }
                                }
                                FTFileUtils.zipFiles(new File(tmpBuildPath).listFiles(), new File(zipBuildPath));
                            }
                            Logger.debug("ZipSourceMap:" + assembleTaskName + " finish, zipPath:" + model.getZipPath());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            Task uploadTask = project.getTasks().create("ft" + capVariantName + "UploadSymbolMap", task -> {
            }).doLast(task -> {

                ProductFlavorModel model = getFlavorModelFromName(variantName);
                if (!model.isAutoUploadMap() && !model.isAutoUploadNativeDebugSymbol()) {
                    return;
                }
                String zipBuildPath = model.getZipPath();
                // Delete previous cache
                try {
                    ObfuscationSettingConfig config = obfuscationSettingMap.get(assembleTaskName);
                    if (config != null) {
                        uploadWithParams(config, model, zipBuildPath);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

            });

            if (!applicationVariant.getName().endsWith("Debug")) {
                project.afterEvaluate(p -> {
                    ProductFlavorModel model = getFlavorModelFromName(variantName);
                    p.getTasks().getAt(assembleTaskName).finalizedBy(zipTask);
                    if (!model.isGenerateSourceMapOnly()) {
                        p.getTasks().getAt(assembleTaskName).finalizedBy(uploadTask);
                    }
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
                        } else {
                            obfuscationSettingMap.put(task.getName(), new ObfuscationSettingConfig());
                            Logger.error("MinifyEnabled or Proguard Setting not found");
                        }
                    }
                }
            });
        });

    }

    /**
     * Delete folder
     *
     * @param file
     * @throws IOException
     */
    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList != null) {
                for (File subFile : fileList) {
                    deleteRecursively(subFile);
                }
            }
        }
        FileUtils.delete(file);  // Use FileUtils to delete file or folder
    }


    /**
     * Scan and add native cmake build path
     *
     * @param p
     * @param list
     * @param nativeLibPath
     */
    private void appendSymbolPath(Project p, ArrayList<String> list, String variantName, String nativeLibPath) {

        p.getAllprojects().forEach(subProject -> {
            File projectPath = subProject.getProjectDir();
            String debugSymbolPath = "";
            if (nativeLibPath.isEmpty()) {
                debugSymbolPath = new File(projectPath, MERGED_LIB_PATH + "/" + variantName + "/out/lib").getAbsolutePath();
            } else {
                debugSymbolPath = new File(projectPath, nativeLibPath).getAbsolutePath();
            }
            File file = new File(debugSymbolPath);
            if (file.exists()) {
                Logger.debug("debugSymbolPath:" + debugSymbolPath);
                list.add(debugSymbolPath);
            }
        });

        //unity symbol
        String rootPath = p.getRootDir().getAbsolutePath();
        String unitySymbolsPath = rootPath + UNITY_SYMBOLS_PATH;
        File unitySymbols = new File(unitySymbolsPath);
        if (unitySymbols.exists()) {
            Logger.debug("unitySymbolsPath:" + unitySymbolsPath);
            list.add(unitySymbolsPath);
        }
    }


    /**
     * Upload symbol file
     *
     * @param settingConfig  obfuscation configuration
     * @throws IOException
     * @throws InterruptedException
     */
    private void uploadWithParams(ObfuscationSettingConfig settingConfig, ProductFlavorModel model, String zipBuildPath) throws IOException, InterruptedException {
        Logger.debug("uploadWithParams:" + model.toString());
        String cmd = "curl -m 1800 -X PUT " + model.getDatakitUrl() + "/v1/sourcemap?"
                + "app_id=" + model.getAppId()
                + "&env=" + model.getEnv()
                + "&version=" + settingConfig.versionName
                + "&platform=android"
                + "&token=" + model.getDatawayToken() +
                " -F file=@" + zipBuildPath + " -H Content-Type:multipart/form-data";

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
            Logger.error("map file upload failed");
            Logger.error("exit code::" + exitCode);
        }

        process.destroy();
    }

    private ProductFlavorModel getFlavorModelFromName(String variantName) {
        ProductFlavorModel model = flavorModelHashMap
                .get(variantName.replace("Release", ""));
        if (model != null) {
            //multiple flavor
            model.mergeFTExtension(extension);
            return model;
        } else {
            //not set flavor, here variantName = release
            model = new ProductFlavorModel(variantName);
            model.setFromFTExtension(extension);
        }
        return model;
    }


    /**
     * obfuscation configuration
     */
    static class ObfuscationSettingConfig {
        /**
         * Package name
         */
        String applicationId = "";
        /**
         * Version name, e.g. 1.0.0
         */
        String versionName = "";
        /**
         * Build Code
         */
        int versionCode;
        /**
         * map output path
         */
        String mappingOutputPath = "";

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
