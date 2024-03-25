package com.ft.sdk.tests;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.garble.manager.LogFileHelper;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.test.base.FTBaseTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * 内部日志管理测试用例，测试追加日志和文件缓存管理
 * {@link com.ft.sdk.garble.manager.LogFileHelper}
 */
public class InnerLogTest extends FTBaseTest {

    private static final String TAG = "InnerLogTest";
    public static final String INNER_LOG_LOG_FILE = "InnerLog.log";
    public static final String INSERT_LOG_CONTENT = "appendLogTest";
    /**
     * sample：2024-03-25 08:54:22:386 E InnerLogTest 111111111
     * 刚好 {@link LogFileHelper#TEST_SPLIT_FILE_SIZE}
     *
     */
    public static final String TEST_DATA = "111111111";

    private File logFile;

    private File logBackFile;

    /**
     * {@link LogUtils#registerInnerLogCacheToFile(File, boolean)}
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        Context application = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        logFile = new File(application.getFilesDir(), INNER_LOG_LOG_FILE);
        logBackFile = new File(application.getFilesDir(), LogFileHelper.LOG_BACKUP_CACHE_PATH);
        Whitebox.invokeMethod(LogUtils.class, logFile, true);
    }


    /**
     * 检验文件内容是否写入成功，检验文件是否生成
     *
     * @throws IOException
     */
    @Test
    public void appendLogTest() throws IOException {
        LogUtils.e(TAG, INSERT_LOG_CONTENT);
        Assert.assertTrue(logFile.exists());

        String content = Utils.readFile(logFile);
        Assert.assertTrue(content.contains(INSERT_LOG_CONTENT));
    }

    /**
     * 日志分离校验
     * {@link com.ft.sdk.garble.manager.LogFileHelper#TEST_SPLIT_FILE_SIZE}
     */
    @Test
    public void splitLogFile() {
        LogUtils.e(TAG, TEST_DATA);
        LogUtils.e(TAG, TEST_DATA);
        Assert.assertTrue(logBackFile.exists());
        Assert.assertTrue(Objects.requireNonNull(logBackFile.listFiles()).length > 0);
    }

    /**
     * 达到总缓存大小
     * {@link com.ft.sdk.garble.manager.LogFileHelper#TEST_CACHE_MAX_TOTAL_SIZE}
     */
    @Test
    public void reachLogLimit() {
        LogUtils.e(TAG, TEST_DATA);
        LogUtils.e(TAG, TEST_DATA);
        LogUtils.e(TAG, TEST_DATA);


        File[] logFiles = logBackFile.listFiles();
        long totalSize = 0;
        if (logFiles != null) {
            for (File file : logFiles) {
                totalSize += file.length();
            }
        }

        Assert.assertTrue(totalSize <= LogFileHelper.TEST_CACHE_MAX_TOTAL_SIZE);
    }

    @Override
    public void tearDown() {
        super.tearDown();
        //删除日志文件
        Utils.deleteFile(logFile.getAbsolutePath());
        Utils.deleteFile(logBackFile.getAbsolutePath());
    }
}
