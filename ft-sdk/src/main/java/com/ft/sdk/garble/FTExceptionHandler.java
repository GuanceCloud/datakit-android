package com.ft.sdk.garble;

import androidx.annotation.NonNull;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.LogUtils;
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

    private boolean canTrackCrash;
    private String env;
    private String trackServiceName;
    private static FTExceptionHandler instance;
    private Thread.UncaughtExceptionHandler mDefaultExceptionHandler;
    private boolean trackConsoleLog;
    private boolean isAndroidTest = false;

    public void uploadCrashLog(String crash) {
        uploadCrashLog(crash, System.currentTimeMillis());
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

    public void initParams(FTSDKConfig ftsdkConfig) {
        if (ftsdkConfig != null) {
            this.canTrackCrash = ftsdkConfig.isEnableTrackAppCrash();
            this.env = ftsdkConfig.getEnv();
            this.trackServiceName = ftsdkConfig.getTraceServiceName();
            this.trackConsoleLog = ftsdkConfig.isTraceConsoleLog();
        }
    }

    public String getEnv() {
        return env;
    }

    public String getTrackServiceName() {
        return trackServiceName;
    }

    public boolean isTrackConsoleLog() {
        return trackConsoleLog;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        if (canTrackCrash) {
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
            uploadCrashLog(result);
        }

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
                    LogUtils.e("BrandonTest", "Close here 2");
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(10);
                } catch (Exception ex2) {

                }
            }
        }

    }

    private void uploadCrashLog(String crash, long timeLine) {
        FTAutoTrack.appCrash();
        LogUtils.d("FTExceptionHandler", "crash=" + crash);
        LogBean logBean = new LogBean(Utils.translateFieldValue(crash), timeLine);
        logBean.setStatus(Status.CRITICAL);
        logBean.setEnv(env);
        logBean.setServiceName(trackServiceName);
        FTTrackInner.getInstance().logBackground(logBean);
    }

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
                            long crashTime = file.lastModified();
                            uploadCrashLog(crashString, crashTime);
                            if (item.getName().contains("anr")) {
                                FTAutoTrack.appAnr(crashTime);
                            } else if (item.getName().contains("native")) {
                                FTAutoTrack.appCrash(crashTime);
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
