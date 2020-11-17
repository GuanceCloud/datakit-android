package com.ft.plugin.garble;

import com.android.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class FTFileUtils {
    public static void copyDifferentFolderFilesIntoOne(String mergedFolderStr,
                                                       String... foldersStr) {
        final File mergedFolder = new File(mergedFolderStr);
        final Map<String, File> filesMap = new HashMap<String, File>();
        for (String folder : foldersStr) {
            updateFilesMap(new File(folder), filesMap, null);
        }

        for (final Map.Entry<String, File> fileEntry : filesMap.entrySet()) {
            final String relativeName = fileEntry.getKey();
            final File srcFile = fileEntry.getValue();
            try {
                File targetFile = new File(mergedFolder, relativeName);
                File parentFile = targetFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }

                FileUtils.copyFile(srcFile, targetFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void updateFilesMap(final File baseFolder, final Map<String, File> filesMap,
                                       final String relativeName) {
        for (final File file : baseFolder.listFiles()) {
            final String fileRelativeName = getFileRelativeName(relativeName, file.getName());

            if (file.isDirectory()) {
                updateFilesMap(file, filesMap, fileRelativeName);
            } else {
                final File existingFile = filesMap.get(fileRelativeName);
                if (existingFile == null || file.lastModified() > existingFile.lastModified()) {
                    filesMap.put(fileRelativeName, file);
                }
            }
        }
    }

    private static String getFileRelativeName(final String baseName, final String fileName) {
        return baseName == null ? fileName : baseName + "/" + fileName;
    }

    /**
     * 压缩一个目录。
     *
     * @param srcDir    需要压缩的目录
     * @param zipFile   压缩后的文件
     * @param overwrite 是否覆盖已存在文件
     * @throws IOException
     */
    public static void zipDirectory(File srcDir, File zipFile, boolean overwrite) throws IOException {
        if (zipFile == null || srcDir == null) {
            throw new IllegalArgumentException("zipFile和srcDir不能为空!");
        }
        if (!overwrite && zipFile.exists()) {
            throw new IOException(zipFile.getAbsolutePath() + "文件已存在，参数设定了不能覆盖。");
        }
        if (!zipFile.exists()) {
            zipFile.createNewFile();
        }

        ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(zipFile));
        zipDirectory(zipOutput, srcDir, srcDir.getName());
        zipOutput.close();
    }

    /**
     * 压缩目录的辅助方法。递归检查子目录。
     *
     * @param zipOutput zip输出流
     * @param file      当前文件
     * @param base      当前文件在压缩包里的绝对名称
     * @throws IOException
     */
    private static void zipDirectory(ZipOutputStream zipOutput, File file, String base) throws IOException {
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            zipOutput.putNextEntry(new ZipEntry(base + "/"));
            base = (base.length() == 0) ? "" : base + "/";
            for (File childFile : fileList) {
                zipDirectory(zipOutput, childFile, base + childFile.getName());
            }
        } else {
            zipOutput.putNextEntry(new ZipEntry(base));
            FileInputStream fileInputStream = new FileInputStream(file);
            int length;
            byte[] buffer = new byte[2048];
            while ((length = fileInputStream.read(buffer)) != -1) {
                zipOutput.write(buffer, 0, length);
                zipOutput.flush();
            }
            fileInputStream.close();
        }
    }

    /**
     * 压缩一个目录。如果存在同名文件，则会覆盖
     *
     * @param srcDir  需要压缩 的目录
     * @param zipFile 压缩产生的文件
     * @throws IOException
     */
    public static void zipDirectory(File srcDir, File zipFile) throws IOException {
        zipDirectory(srcDir, zipFile, true);
    }

    /**
     * 压缩一个目录。压缩输出的文件名为(目录名.zip)
     *
     * @param srcDir 需要压缩的目录
     * @throws IOException
     */
    public static void zipDirectory(File srcDir) throws IOException {
        zipDirectory(srcDir, new File(srcDir.getAbsolutePath() + ".zip"), true);
    }
}
