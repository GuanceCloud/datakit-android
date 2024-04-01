package com.ft.sdk;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.threadpool.DataUploaderThreadPool;
import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;
import com.ft.sdk.garble.threadpool.RunnerCompleteCallBack;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;

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

    /**
     * 是否是之前的崩溃的数据，{@link #NATIVE_CALLBACK_VERSION } 以后的版本会对之后补充的崩溃信息进行标记
     */
    public static final String IS_PRE_CRASH = "is_pre_crash";


    /**
     * 可以接受到 native crash 回调的版本
     */
    public static final String NATIVE_CALLBACK_VERSION = "1.1.0-alpha01";

    private static FTExceptionHandler instance;
    private final Thread.UncaughtExceptionHandler mDefaultExceptionHandler;
    /**
     * 注意 ：AndroidTest 会调用这个方法 {@link com.ft.test.base.FTBaseTest#avoidCrash()}
     */
    private boolean isAndroidTest = false;

    /**
     * 上传崩溃日志，根据 {@link FTRUMConfig#isRumEnable(),FTRUMConfig#isEnableTrackAppCrash()} 进行判断
     * <p>
     * 这里线程调用的路径是 FTEventCsr -> FTDataUp -> FTEventCsr
     *
     * @param crash    崩溃日志简述
     * @param message  崩溃堆栈
     * @param state    app 运行状态 {@link  AppState}
     * @param callBack
     */
    public void uploadCrashLog(String crash, String message, AppState state, RunnerCompleteCallBack callBack) {
        if (config.isRumEnable() &&
                config.isEnableTrackAppCrash()) {
            long dateline = Utils.getCurrentNanoTime();
            FTRUMInnerManager.get().addError(crash, message, dateline, ErrorType.JAVA.toString(), state, callBack);
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
     * 会重新将异常内容抛出，避免集成方正常的异常捕获逻辑，异常数据会{@link #uploadCrashLog(String, String, AppState, RunnerCompleteCallBack)} )}
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
        uploadCrashLog(result, e.getMessage(), FTActivityManager.get().getAppState(), new RunnerCompleteCallBack() {
            @Override
            public void onComplete() {
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
        });

    }

    /**
     * 检测并上传 native dump 文件
     *
     * @param nativeDumpPath 生成 ANR 或 Native Crash tombstone 文件路径
     */
    public void checkAndSyncPreDump(final String nativeDumpPath, RunnerCompleteCallBack callBack) {
        DataUploaderThreadPool.get().execute(new Runnable() {
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
                                String value = Utils.readSectionValueFromDump(item.getAbsolutePath(), DUMP_FILE_KEY_APP_STATE);
                                uploadNativeCrash(item, AppState.getValueFrom(value), true, callBack);
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
     * 在消费队列中，进行 Native Crash 的上传
     * <p>
     * 这里会有 FTEventCsr 内线程嵌套，但是不太可能存在线程阻塞，这里是为了加载崩溃日志内容和写入 Error 数据，
     * 在 EventConsumerThreadPool 顺序执行. 线程执行路径为  FTEventCsr-> FTEventCsr -> FTDataUp -> FTEventCsr
     *
     * @param item       crash 日志内容文件
     * @param state      应用状态
     * @param isPreCrash 是否是前一次异常数据，false 代表上传的是当下崩溃的信息
     * @param callBack
     */
    public void uploadNativeCrashBackground(File item, AppState state, boolean isPreCrash, RunnerCompleteCallBack callBack) {
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    uploadNativeCrash(item, state, isPreCrash, callBack);
                } catch (IOException e) {
                    LogUtils.e(TAG, Log.getStackTraceString(e));
                }
            }
        });
    }

    /**
     * 上传 Native Crash
     *
     * @param item
     * @param state
     * @param isPreCrash true 记录前一次的崩溃数据，反之是当下的崩溃数据
     * @param callBack
     * @throws IOException
     */
    private void uploadNativeCrash(File item, AppState state, boolean isPreCrash, RunnerCompleteCallBack callBack) throws IOException {
        String crashString = Utils.readFile(item.getAbsolutePath(), Charset.defaultCharset());
        long crashTime = item.lastModified() * 1000000L;
        HashMap<String, Object> property = new HashMap<>();
        property.put(IS_PRE_CRASH, isPreCrash);
        if (config.isEnableTrackAppANR()
                && item.getName().contains(ANR_FILE_NAME)) {
            FTRUMInnerManager.get().addError(crashString, "Native Crash",
                    crashTime, ErrorType.ANR_CRASH.toString(), state, property, callBack);
        } else if (config.isEnableTrackAppCrash()
                && item.getName().contains(NATIVE_FILE_NAME)) {
            FTRUMInnerManager.get().addError(crashString, "Native Crash",
                    crashTime, ErrorType.NATIVE.toString(), state, property, callBack);
        }

    }


    /**
     * 释放对象，也就是会舍弃 {@link #config} 的配置
     */
    public static void release() {
        instance = null;
    }
}
