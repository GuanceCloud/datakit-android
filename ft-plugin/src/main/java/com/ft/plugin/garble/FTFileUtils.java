package com.ft.plugin.garble;



import org.gradle.internal.impldep.org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件工具类
 */
class FTFileUtils {
    /**
     * 复制合并文件
     *
     * @param mergedFolderStr
     * @param foldersStr
     */
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

    /**
     * 上传 map 文件
     * @param baseFolder
     * @param filesMap
     * @param relativeName
     */
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

    /**
     * 获取文件相对地址
     * @param baseName
     * @param fileName
     * @return
     */
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


    /**
     * 对一个文件数组进行压缩。数组长度必须大于0
     *
     * @param files     文件数组。长度必须大于0
     * @param zipFile   压缩后产生的文件
     * @param overwrite 如存在同名文件，是否覆盖
     * @throws IOException
     */
    public static void zipFiles(File[] files, File zipFile, boolean overwrite) throws IOException {
        if (zipFile == null || files == null) {
            throw new IllegalArgumentException("zipFile和srcDir不能为空!");
        }
        if (files.length == 0) {
            throw new IOException("不能对一个空的文件列表进行压缩");
        }
        if (!overwrite && zipFile.exists()) {
            throw new IOException(zipFile.getAbsolutePath() + "文件已存在，参数设定了不能覆盖。");
        }
        if (!zipFile.exists()) {
            zipFile.createNewFile();
        }

        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
        byte[] buffer = new byte[2048];
        int length;
        for (File file : files) {
            if (file.isDirectory()) {
                zipDirectory(zipOutputStream, file, file.getName());
            } else {
                FileInputStream fileInputStream = new FileInputStream(file);
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                while ((length = fileInputStream.read(buffer)) != -1) {
                    zipOutputStream.write(buffer, 0, length);
                    zipOutputStream.flush();
                }
                fileInputStream.close();
            }

        }

        zipOutputStream.close();
    }

    /**
     * 对一个文件列表进行压缩。列表长度长度必须大于0
     *
     * @param fileList  文件列表。长度必须大于0
     * @param zipFile   压缩后产生的文件
     * @param overwrite 如果已经存在同名文件，是否覆盖。
     * @throws IOException
     */
    public static void zipFiles(List<File> fileList, File zipFile, boolean overwrite) throws IOException {
        zipFiles(fileList.toArray(new File[fileList.size()]), zipFile, overwrite);
    }

    /**
     * 对一个文件列表进行压缩，列表长度长度必须大于0。如存在同名目标文件，则会覆盖它。
     *
     * @param fileList 文件列表。长度必须大于0
     * @param zipFile  压缩后生成的文件
     * @throws IOException
     */
    public static void zipFiles(List<File> fileList, File zipFile) throws IOException {
        zipFiles(fileList.toArray(new File[fileList.size()]), zipFile, true);
    }

    /**
     * 对一个文件数组进行压缩，数组长度必须大于0。如存在同名目标文件，则会覆盖它。
     *
     * @param files   文件数组。长度必须大于0
     * @param zipFile 压缩后生成的文件
     * @throws IOException
     */
    public static void zipFiles(File[] files, File zipFile) throws IOException {
        zipFiles(files, zipFile, true);
    }

    /**
     * 复制文件
     *
     * @param src
     * @param tar
     * @return
     */

    public static boolean copyFile(File src, File tar) {

        if (!tar.getParentFile().exists()) {
            tar.getParentFile().mkdirs();
        }

        if (!tar.exists()) {
            try {
                tar.createNewFile();
            } catch (IOException e) {

            }
        }

        try {
            if (src.isFile()) {

                InputStream is = new FileInputStream(src);
                OutputStream op = new FileOutputStream(tar);
                BufferedInputStream bis = new BufferedInputStream(is);
                BufferedOutputStream bos = new BufferedOutputStream(op);
                byte[] bt = new byte[8192];
                int len = bis.read(bt);
                while (len != -1) {
                    bos.write(bt, 0, len);
                    len = bis.read(bt);
                }
                bis.close();
                bos.close();
            }
            if (src.isDirectory()) {
                File[] f = src.listFiles();
                tar.mkdir();
                for (int i = 0; i < f.length; i++) {
                    copyFile(f[i].getAbsoluteFile(),
                            new File(tar.getAbsoluteFile() + File.separator
                                    + f[i].getName())
                    );
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
