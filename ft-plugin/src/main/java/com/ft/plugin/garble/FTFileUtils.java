package com.ft.plugin.garble;




import org.apache.commons.io.FileUtils;

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
 * File utility class
 */
class FTFileUtils {
    /**
     * Copy and merge files
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
     * Upload map file
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
     * Get file relative path
     * @param baseName
     * @param fileName
     * @return
     */
    private static String getFileRelativeName(final String baseName, final String fileName) {
        return baseName == null ? fileName : baseName + "/" + fileName;
    }

    /**
     * Compress a directory.
     *
     * @param srcDir    Directory to be compressed
     * @param zipFile   Compressed file
     * @param overwrite Whether to overwrite existing file
     * @throws IOException
     */
    public static void zipDirectory(File srcDir, File zipFile, boolean overwrite) throws IOException {
        if (zipFile == null || srcDir == null) {
            throw new IllegalArgumentException("zipFile and srcDir cannot be empty!");
        }
        if (!overwrite && zipFile.exists()) {
            throw new IOException(zipFile.getAbsolutePath() + " file already exists, parameter set to not overwrite.");
        }
        if (!zipFile.exists()) {
            zipFile.createNewFile();
        }

        ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(zipFile));
        zipDirectory(zipOutput, srcDir, srcDir.getName());
        zipOutput.close();
    }

    /**
     * Auxiliary method for compressing directories. Recursively checks subdirectories.
     *
     * @param zipOutput zip output stream
     * @param file      Current file
     * @param base      Absolute name of the current file in the zip package
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
     * Compress a directory. If a file with the same name exists, it will be overwritten
     *
     * @param srcDir  Directory to be compressed
     * @param zipFile Compressed file generated
     * @throws IOException
     */
    public static void zipDirectory(File srcDir, File zipFile) throws IOException {
        zipDirectory(srcDir, zipFile, true);
    }

    /**
     * Compress a directory. The output file name is (directory name.zip)
     *
     * @param srcDir Directory to be compressed
     * @throws IOException
     */
    public static void zipDirectory(File srcDir) throws IOException {
        zipDirectory(srcDir, new File(srcDir.getAbsolutePath() + ".zip"), true);
    }


    /**
     * Compress an array of files. The array length must be greater than 0
     *
     * @param files     Array of files. Length must be greater than 0
     * @param zipFile   Compressed file generated
     * @param overwrite If a file with the same name exists, whether to overwrite
     * @throws IOException
     */
    public static void zipFiles(File[] files, File zipFile, boolean overwrite) throws IOException {
        if (zipFile == null || files == null) {
            throw new IllegalArgumentException("zipFile and srcDir cannot be empty!");
        }
        if (files.length == 0) {
            throw new IOException("Cannot compress an empty file list");
        }
        if (!overwrite && zipFile.exists()) {
            throw new IOException(zipFile.getAbsolutePath() + " file already exists, parameter set to not overwrite.");
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
     * Compress a list of files. The list length must be greater than 0
     *
     * @param fileList  List of files. Length must be greater than 0
     * @param zipFile   Compressed file generated
     * @param overwrite If a file with the same name exists, whether to overwrite.
     * @throws IOException
     */
    public static void zipFiles(List<File> fileList, File zipFile, boolean overwrite) throws IOException {
        zipFiles(fileList.toArray(new File[fileList.size()]), zipFile, overwrite);
    }

    /**
     * Compress a list of files. The list length must be greater than 0. If a file with the same name exists, it will be overwritten.
     *
     * @param fileList List of files. Length must be greater than 0
     * @param zipFile  Compressed file generated
     * @throws IOException
     */
    public static void zipFiles(List<File> fileList, File zipFile) throws IOException {
        zipFiles(fileList.toArray(new File[fileList.size()]), zipFile, true);
    }

    /**
     * Compress an array of files. The array length must be greater than 0. If a file with the same name exists, it will be overwritten.
     *
     * @param files   Array of files. Length must be greater than 0
     * @param zipFile Compressed file generated
     * @throws IOException
     */
    public static void zipFiles(File[] files, File zipFile) throws IOException {
        zipFiles(files, zipFile, true);
    }

    /**
     * Copy file
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
