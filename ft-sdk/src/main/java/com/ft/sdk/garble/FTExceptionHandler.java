package com.ft.sdk.garble;

import androidx.annotation.NonNull;

import com.ft.sdk.BuildConfig;
import com.ft.sdk.FTTrack;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * create: by huangDianHua
 * time: 2020/6/1 13:56:26
 * description:崩溃日志处理
 */
public class FTExceptionHandler implements Thread.UncaughtExceptionHandler {
    private boolean canTrackCrash;
    private String env = BuildConfig.BUILD_TYPE;
    private static FTExceptionHandler instance;
    private Thread.UncaughtExceptionHandler mDefaultExceptionHandler;
    private FTExceptionHandler(){
        mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
    public static FTExceptionHandler get(){
        if(instance == null){
            instance = new FTExceptionHandler();
        }
        return instance;
    }

    /**
     * 设置是否开启崩溃日志捕获
     * @param enable
     */
    public void enableTrackCrash(boolean enable){
        this.canTrackCrash = enable;
    }

    /**
     * 设置崩溃日志的环境
     * @param env
     */
    public void crashEvn(String env){
        this.env = env;
    }
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        if(canTrackCrash) {
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
            LogBean logBean = new LogBean(Constants.USER_AGENT,Utils.translateFieldValue(result),System.currentTimeMillis());
            logBean.setStatus(Status.CRITICAL);
            logBean.setEnv(env);
            logBean.setServiceName("dataflux sdk");
            FTTrack.getInstance().logBackground(logBean);
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        if(mDefaultExceptionHandler != null){
            try{
                mDefaultExceptionHandler.uncaughtException(t,e);
            }catch (Exception ex){}
        }else {
            try {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
            }catch (Exception ex2){

            }
        }
    }
}
