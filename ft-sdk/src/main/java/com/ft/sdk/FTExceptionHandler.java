package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
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
    private static final String TAG = "FTExceptionHandler";
    private static final String EXCEPTION_FILE_PREFIX_TOMBSTONE = "tombstone";
    private static final String ANR_FILE_NAME = "anr";
    private static final String NATIVE_FILE_NAME = "native";
    private static final String APP_STATE_RUNNING = "running";
    private static final String DUMP_FILE_KEY_APP_STATE = "appState";

    private static FTExceptionHandler instance;
    private Thread.UncaughtExceptionHandler mDefaultExceptionHandler;
    private boolean isAndroidTest = false;

    public void uploadCrashLog(String crash, String message, AppState state) {
        if (config.isRumEnable() &&
                config.isEnableTrackAppCrash()) {
            long dateline = Utils.getCurrentNanoTime();
            FTAutoTrack.putRUMError(crash, message, dateline, ErrorType.JAVA, state);
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

    public void initConfig(FTRUMConfig config) {
        this.config = config;
    }


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
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        if (isAndroidTest) {
            e.printStackTrace();
        } else {
            if (mDefaultExceptionHandler != null) {
                try {

                    mDefaultExceptionHandler.uncaughtException(t, e);
                } catch (Exception ex) {
                }
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
    public void checkAndSyncPreDump(String nativeDumpPath) {
        ThreadPoolUtils.get().execute(() -> {
            File file = new File(nativeDumpPath);
            if (!file.exists()) {
                return;
            }
            File[] list = file.listFiles();
            if (list != null && list.length > 0) {
                for (File item : list) {

                    if (item.getName().startsWith(EXCEPTION_FILE_PREFIX_TOMBSTONE)) {
                        try {
                            String crashString = Utils.readFile(item.getAbsolutePath(), Charset.defaultCharset());
                            long crashTime = file.lastModified() * 1000000L;

                            String value = Utils.readSectionValueFromDump(item.getAbsolutePath(), DUMP_FILE_KEY_APP_STATE);

                            if (config.isRumEnable() && config.isEnableTrackAppCrash()) {
                                if (item.getName().contains(ANR_FILE_NAME)) {
//                                    FTAutoTrack.putRUMAnr(crashString, crashTime);
                                } else if (item.getName().contains(NATIVE_FILE_NAME)) {
                                    FTAutoTrack.putRUMError(crashString, "Native Crash", crashTime, ErrorType.NATIVE, AppState.getValueFrom(value));
                                }
                            }


                            Utils.deleteFile(item.getAbsolutePath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }


    public static void release() {
        instance = null;
    }
}
