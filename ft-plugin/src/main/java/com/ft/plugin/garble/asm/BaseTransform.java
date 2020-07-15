package com.ft.plugin.garble.asm;

import com.android.build.api.transform.Context;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.ide.common.internal.WaitableExecutor;
import com.ft.plugin.garble.Logger;
import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * From https://github.com/Leaking/Hunter/blob/master/hunter-transform/src/main/java/com/quinn/hunter/transform/HunterTransform.java
 * DATE:2019-11-29 13:33
 * Description:字节码转换基类
 */
public class BaseTransform extends Transform {
    private Project project;
    protected BaseWeaver bytecodeWeaver;
    private WaitableExecutor waitableExecutor;

    public BaseTransform(Project project) {
        this.project = project;
        this.waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<QualifiedContent.ScopeType> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        transformFun(transformInvocation.getContext(),
                transformInvocation.getInputs(),
                transformInvocation.getReferencedInputs(),
                transformInvocation.getOutputProvider(),
                transformInvocation.isIncremental());
    }

    private void transformFun(Context context,
                              Collection<TransformInput> inputs,
                              Collection<TransformInput> referencedInputs,
                              TransformOutputProvider outputProvider,
                              boolean isIncremental) throws IOException, TransformException, InterruptedException {

        Logger.info(getName() + " isIncremental = " + isIncremental);
        long startTime = System.currentTimeMillis();
        if (!isIncremental) {
            outputProvider.deleteAll();
        }
        URLClassLoader urlClassLoader = ClassLoaderHelper.getClassLoader(inputs, referencedInputs, project);
        this.bytecodeWeaver.setClassLoader(urlClassLoader);
        boolean flagForCleanDexBuilderFolder = false;
        for (TransformInput input : inputs) {
            for (JarInput jarInput : input.getJarInputs()) {
                //logger.warn("jarInput.getFile().getAbsolutePath() = " + jarInput.getFile().getAbsolutePath());
                Status status = jarInput.getStatus();
                File dest = outputProvider.getContentLocation(
                        jarInput.getFile().getAbsolutePath(),
                        jarInput.getContentTypes(),
                        jarInput.getScopes(),
                        Format.JAR);
                if (isIncremental) {
                    switch (status) {
                        case NOTCHANGED:
                            break;
                        case ADDED:
                        case CHANGED:
                            transformJar(jarInput.getFile(), dest, status);
                            break;
                        case REMOVED:
                            if (dest.exists()) {
                                FileUtils.forceDelete(dest);
                            }
                            break;
                    }
                } else {
                    if (!isIncremental && !flagForCleanDexBuilderFolder) {
                        cleanDexBuilderFolder(dest);
                        flagForCleanDexBuilderFolder = true;
                    }
                    transformJar(jarInput.getFile(), dest, status);
                }
            }

            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File dest = outputProvider.getContentLocation(directoryInput.getName(),
                        directoryInput.getContentTypes(), directoryInput.getScopes(),
                        Format.DIRECTORY);
                FileUtils.forceMkdir(dest);
                if (isIncremental) {
                    String srcDirPath = directoryInput.getFile().getAbsolutePath();
                    String destDirPath = dest.getAbsolutePath();
                    Map<File, Status> fileStatusMap = directoryInput.getChangedFiles();
                    for (Map.Entry<File, Status> changedFile : fileStatusMap.entrySet()) {
                        Status status = changedFile.getValue();
                        File inputFile = changedFile.getKey();
                        String destFilePath = inputFile.getAbsolutePath().replace(srcDirPath, destDirPath);
                        File destFile = new File(destFilePath);
                        switch (status) {
                            case NOTCHANGED:
                                break;
                            case REMOVED:
                                if (destFile.exists()) {
                                    //noinspection ResultOfMethodCallIgnored
                                    destFile.delete();
                                }
                                break;
                            case ADDED:
                            case CHANGED:
                                try {
                                    FileUtils.touch(destFile);
                                } catch (IOException e) {
                                    //maybe mkdirs fail for some strange reason, try again.
                                    Files.createParentDirs(destFile);
                                }
                                transformSingleFile(inputFile, destFile, srcDirPath);
                                break;
                        }
                    }
                } else {
                    transformDir(directoryInput.getFile(), dest);
                }

            }

        }

        waitableExecutor.waitForTasksWithQuickFail(true);
        long costTime = System.currentTimeMillis() - startTime;
        Logger.info((getName() + " cost " + costTime + "ms"));
    }

    private void transformSingleFile(final File inputFile, final File outputFile, final String srcBaseDir) {
        waitableExecutor.execute(() -> {
            bytecodeWeaver.weaveSingleClassToFile(inputFile, outputFile, srcBaseDir);
            return null;
        });
    }

    private void transformDir(final File inputDir, final File outputDir) throws IOException {
        final String inputDirPath = inputDir.getAbsolutePath();
        final String outputDirPath = outputDir.getAbsolutePath();
        if (inputDir.isDirectory()) {
            for (final File file : com.android.utils.FileUtils.getAllFiles(inputDir)) {
                waitableExecutor.execute(() -> {
                    String filePath = file.getAbsolutePath();
                    File outputFile = new File(filePath.replace(inputDirPath, outputDirPath));
                    try {
                        bytecodeWeaver.weaveSingleClassToFile(file, outputFile, inputDirPath);
                    }catch (Exception e){
                        Logger.info("修改类异常-文件名："+filePath+"----异常原因："+e);
                        throw e.getClass().newInstance();
                    }
                    return null;
                });
            }
        }
    }

    private void transformJar(final File srcJar, final File destJar, Status status) {
        waitableExecutor.execute(() -> {
            bytecodeWeaver.weaveJar(srcJar, destJar);
            return null;
        });
    }

    private void cleanDexBuilderFolder(File dest) {
        waitableExecutor.execute(() -> {
            try {
                String dexBuilderDir = replaceLastPart(dest.getAbsolutePath(), getName(), "dexBuilder");
                //intermediates/transforms/dexBuilder/debug
                File file = new File(dexBuilderDir).getParentFile();
                project.getLogger().warn("clean dexBuilder folder = " + file.getAbsolutePath());
                if (file.exists() && file.isDirectory()) {
                    com.android.utils.FileUtils.deleteDirectoryContents(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    private String replaceLastPart(String originString, String replacement, String toreplace) {
        int start = originString.lastIndexOf(replacement);
        StringBuilder builder = new StringBuilder();
        builder.append(originString.substring(0, start));
        builder.append(toreplace);
        builder.append(originString.substring(start + replacement.length()));
        return builder.toString();
    }

    @Override
    public boolean isCacheable() {
        return true;
    }
}
