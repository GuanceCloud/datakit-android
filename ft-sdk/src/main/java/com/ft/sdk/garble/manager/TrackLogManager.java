
package com.ft.sdk.garble.manager;

import com.ft.sdk.FTTrack;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.ThreadPoolUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * author: huangDianHua
 * time: 2020/7/22 11:16:58
 * description: 本地打印日志同步管理类
 */
public class TrackLogManager {
    private static TrackLogManager instance;
    private List<LogBean> logBeanList = new CopyOnWriteArrayList<>();
    private LinkedBlockingQueue<LogBean> logQueue = new LinkedBlockingQueue<>();
    private volatile boolean isRunning;
    private volatile int count = 0;

    public synchronized void optCount(int optCount) {
        count += optCount;
    }

    private TrackLogManager() {
        count = FTDBManager.get().queryCountLog();
    }

    public static TrackLogManager get() {
        synchronized (TrackLogManager.class) {
            if (instance == null) {
                instance = new TrackLogManager();
            }
            return instance;
        }
    }

    public synchronized void trackLog(LogBean logBean) {
        logQueue.add(logBean);
        rotationSync();
    }

    private void rotationSync() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        FutureTask<Boolean> futureTask = new FutureTask(() -> {
            try {
                //当队列中有数据时，不断执行取数据操作
                LogBean logBean;
                while ((logBean = logQueue.take()) != null) {
                    isRunning = true;
                    logBeanList.add(logBean);//取出数据放到集合中
                    if (logBeanList.size() >= 5) {//当取出的数据大于等于5条时执行插入数据库操作
                        FTTrack.getInstance().logBackgroundSync(logBeanList);
                        logBeanList.clear();//插入完成后执行清除集合操作
                    }
                }
            } catch (Exception e) {
            } finally {
                isRunning = false;
            }
            return true;
        });
        ThreadPoolUtils.get().execute(futureTask);
    }
}
