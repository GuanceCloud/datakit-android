package com.ft.sdk;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * create: by huangDianHua
 * time: 2020/6/1 13:56:26
 * description:崩溃日志处理
 */
public class FTExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTExceptionHandler";

    /**
     * Android tombstone 文件前缀
     */
    private static final String EXCEPTION_FILE_PREFIX_TOMBSTONE = "tombstone";

    /**
     * Android ANR 文件名内包含字符，暂不使用
     */
    private static final String ANR_FILE_NAME = "anr";

    /**
     * Android Native 文件包含字符
     */
    private static final String NATIVE_FILE_NAME = "native";

    /**
     * 判断 App 运行状态字段
     */
    private static final String DUMP_FILE_KEY_APP_STATE = "appState";

    private static FTExceptionHandler instance;
    private final Thread.UncaughtExceptionHandler mDefaultExceptionHandler;
    /**
     * 用于测试用例
     */
    private boolean isAndroidTest = false;

    /**
     * 上传崩溃日志，根据 {@link FTRUMConfig#isRumEnable(),FTRUMConfig#isEnableTrackAppCrash()} 进行判断
     *
     * @param crash   崩溃日志简述
     * @param message 崩溃堆栈
     * @param state   app 运行状态 {@link  AppState}
     */
    public void uploadCrashLog(String crash, String message, AppState state) {
        if (config.isRumEnable() &&
                config.isEnableTrackAppCrash()) {
            long dateline = Utils.getCurrentNanoTime();
            FTRUMInnerManager.get().addError(crash, message, dateline, ErrorType.JAVA.toString(), state);
        }
    }

    private FTExceptionHandler() {
        mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static FTExceptionHandler get() {
        if (instance == null) {
            instance = new FTExceptionHandler();
        }
        return instance;
    }

    private FTRUMConfig config;

    /**
     * 初始化 {@link FTRUMConfig},在 {@link FTSdk#initRUMWithConfig(FTRUMConfig)} } 中惊醒
     *
     * @param config
     */
    void initConfig(FTRUMConfig config) {
        this.config = config;
    }


    /**
     * 抓取全局未捕获异常 {@link Exception}
     * <p>
     * 此处捕获的是 Java 代码层的异常，不包含 C/C++ 异常，抓取数据后，
     * 会重新将异常内容抛出，避免集成方正常的异常捕获逻辑，异常数据会{@link #uploadCrashLog(String, String, AppState)}
     * 上传异常数据
     *
     * @param t 返回异常线程
     * @param e 返回抛出异常对象
     */
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        Throwable cause = e.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        uploadCrashLog(result, e.getMessage(), FTActivityManager.get().getAppState());

        try {
            //给数据存存储空出一些时间
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        //测试用例直接
        if (isAndroidTest) {
            e.printStackTrace();
        } else {
            if (mDefaultExceptionHandler != null) {
                mDefaultExceptionHandler.uncaughtException(t, e);
            } else {
                try {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(10);
                } catch (Exception ex2) {

                }
            }
        }

    }

//    private void uploadCrashLog(String crash, long timeLine) {
//        LogUtils.d("FTExceptionHandler", "crash=" + crash);
//        LogBean logBean = new LogBean(Utils.translateFieldValue(crash), timeLine);
//        logBean.setStatus(Status.CRITICAL);
//        logBean.setEnv(env);
//        logBean.setServiceName(trackServiceName);
//        FTTrackInner.getInstance().logBackground(logBean);
//    }

    /**
     * 检测并上传 native dump 文件
     *
     * @param nativeDumpPath
     */
    public void checkAndSyncPreDump(final String nativeDumpPath) {
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(nativeDumpPath);
                if (!file.exists()) {
                    return;
                }
                File[] list = file.listFiles();
                if (list != null) {
                    for (File item : list) {

                        if (item.getName().startsWith(EXCEPTION_FILE_PREFIX_TOMBSTONE)) {
                            try {
                                String crashString = Utils.readFile(item.getAbsolutePath(), Charset.defaultCharset());
                                long crashTime = file.lastModified() * 1000000L;

                                String value = Utils.readSectionValueFromDump(item.getAbsolutePath(), DUMP_FILE_KEY_APP_STATE);

                                if (config.isEnableTrackAppANR()
                                        && item.getName().contains(ANR_FILE_NAME)) {
                                    FTRUMInnerManager.get().addError(crashString, "Native Crash",
                                            crashTime, ErrorType.ANR_CRASH.toString(), AppState.getValueFrom(value));
                                } else if (config.isEnableTrackAppCrash()
                                        && item.getName().contains(NATIVE_FILE_NAME)) {
                                    FTRUMInnerManager.get().addError(crashString, "Native Crash",
                                            crashTime, ErrorType.NATIVE.toString(), AppState.getValueFrom(value));
                                }

                                Utils.deleteFile(item.getAbsolutePath());
                            } catch (IOException e) {
                                LogUtils.e(TAG, Log.getStackTraceString(e));
                            }
                        }
                    }
                }
            }
        });
    }


    /**
     *
     */
    public static void release() {
        instance = null;
    }
}
