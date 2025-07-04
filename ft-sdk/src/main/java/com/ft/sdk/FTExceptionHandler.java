package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.threadpool.DataProcessThreadPool;
import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;
import com.ft.sdk.garble.threadpool.RunnerCompleteCallBack;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * create: by huangDianHua
 * time: 2020/6/1 13:56:26
 * description: Crash log handling
 */
public class FTExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTExceptionHandler";

    /**
     * Android tombstone file prefix
     */
    private static final String EXCEPTION_FILE_PREFIX_TOMBSTONE = "tombstone";

    /**
     * Characters contained in Android ANR file names, not used for now
     */
    private static final String ANR_FILE_NAME = "anr";

    /**
     * Characters contained in Android Native files
     */
    private static final String NATIVE_FILE_NAME = "native";

    /**
     * Field to determine App running state
     */
    private static final String DUMP_FILE_KEY_APP_STATE = "appState";

    /**
     * Whether it is data from a previous crash, {@link #NATIVE_CALLBACK_VERSION } later versions will mark crash info added afterwards
     */
    public static final String IS_PRE_CRASH = "is_pre_crash";


    /**
     * Version that can receive native crash callbacks
     */
    public static final String NATIVE_CALLBACK_VERSION = "1.1.0-alpha01";

    /**
     * Version that can accept native logcat line limit
     */
    public static final String NATIVE_LOGCAT_SETTING_VERSION = "1.1.1-alpha01";

    private static FTExceptionHandler instance;
    private Thread.UncaughtExceptionHandler mDefaultExceptionHandler;
    /**
     * Note: AndroidTest will call this method {@link com.ft.test.base.FTBaseTest#avoidCrash()}
     */
    private boolean isAndroidTest = false;

    /**
     * Upload crash log, determined by {@link FTRUMConfig#isRumEnable(),FTRUMConfig#isEnableTrackAppCrash()}
     * <p>
     * The thread call path here is FTEventCsr -> FTDataUp -> FTEventCsr
     *
     * @param crash    Crash log summary
     * @param message  Crash stack
     * @param state    app running state {@link  AppState}
     * @param callBack
     */
    public void uploadCrashLog(String crash, String message, AppState state, RunnerCompleteCallBack callBack) {
        long dateline = Utils.getCurrentNanoTime();
        FTRUMInnerManager.get().addError(crash, message, dateline, ErrorType.JAVA.toString(), state, callBack);
    }

    private FTExceptionHandler() {
    }

    public static FTExceptionHandler get() {
        if (instance == null) {
            instance = new FTExceptionHandler();
        }
        return instance;
    }

    private FTRUMConfig config;

    private boolean registerHandled;

    /**
     * Initialize {@link FTRUMConfig}, called in {@link FTSdk#initRUMWithConfig(FTRUMConfig)}
     *
     * @param config
     */
    void initConfig(FTRUMConfig config) {
        this.config = config;
        if (config.isRumEnable() &&
                config.isEnableTrackAppCrash() && !registerHandled) {
            Thread.UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
            //Avoid duplicate settings
            if (!(currentHandler instanceof FTExceptionHandler)) {
                mDefaultExceptionHandler = currentHandler;
                Thread.setDefaultUncaughtExceptionHandler(this);
                registerHandled = true;
            }
        }
    }


    /**
     * Catch global uncaught exceptions {@link Exception}
     * <p>
     * This catches exceptions at the Java code layer, not including C/C++ exceptions. After catching the data,
     * it will rethrow the exception to avoid interfering with the normal exception catching logic of the integration. Exception data will be uploaded by {@link #uploadCrashLog(String, String, AppState, RunnerCompleteCallBack)}
     *
     * @param t Returned exception thread
     * @param e Returned thrown exception object
     */
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        Throwable cause = e.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        writer.append("\n").append(Utils.getAllThreadStack());
        ExtraLogCatSetting logCatWithError = config.getExtraLogCatWithJavaCrash();
        if (logCatWithError != null) {
            writer.append("\n")
                    .append(Utils.getLogcat(logCatWithError.getLogcatMainLines(),
                            logCatWithError.getLogcatSystemLines(),
                            logCatWithError.getLogcatEventsLines()));
        }
        CountDownLatch latch = new CountDownLatch(1);
        uploadCrashLog(writer.toString(), e.getMessage(), FTActivityManager.get().getAppState(), new RunnerCompleteCallBack() {
            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        try {
            latch.await(800, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(ie));
        }

        if (isAndroidTest) {  //Test case directly
            e.printStackTrace();
        } else {
            if (mDefaultExceptionHandler != null) {
                mDefaultExceptionHandler.uncaughtException(t, e);
            }
        }
    }

    /**
     * Detect and upload native dump files
     *
     * @param nativeDumpPath Path to generated ANR or Native Crash tombstone file
     */
    public void checkAndSyncPreDump(final String nativeDumpPath, RunnerCompleteCallBack callBack) {
        DataProcessThreadPool.get().execute(new Runnable() {
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
                                LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Upload Native Crash in the consumer queue
     * <p>
     * There will be FTEventCsr internal thread nesting here, but thread blocking is unlikely. This is to load crash log content and write Error data,
     * executed sequentially in EventConsumerThreadPool. Thread execution path: FTEventCsr-> FTEventCsr -> FTDataUp -> FTEventCsr
     *
     * @param item       Crash log content file
     * @param state      App state
     * @param isPreCrash Whether it is previous crash data, false means uploading current crash info
     * @param callBack
     */
    public void uploadNativeCrashBackground(File item, AppState state, boolean isPreCrash, RunnerCompleteCallBack callBack) {
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    uploadNativeCrash(item, state, isPreCrash, callBack);
                } catch (Exception e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                    if (callBack != null) {
                        callBack.onComplete();
                    }
                }
            }
        });
    }

    /**
     * Upload Native Crash
     *
     * @param item
     * @param state
     * @param isPreCrash true record the previous crash data, otherwise it is the current crash data
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
     * Release object, which means discarding {@link #config} configuration
     */
    public static void release() {
        if (instance != null) {
            Thread.setDefaultUncaughtExceptionHandler(instance.mDefaultExceptionHandler);
            instance = null;
        }
    }
}
