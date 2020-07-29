
package com.ft.sdk.garble.manager;

import com.ft.sdk.FTTrack;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.FutureTask;

/**
 * author: huangDianHua
 * time: 2020/7/22 11:16:58
 * description: 本地打印日志同步管理类
 */
public class TrackLogManager {
    private static TrackLogManager instance;
    private List<LogBean> logBeanList = new CopyOnWriteArrayList<>();
    private ConcurrentLinkedQueue<LogBean> logQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean isRunning;

    private TrackLogManager() {
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

    private synchronized void rotationSync() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        FutureTask<Boolean> futureTask = new FutureTask(() -> {
            try {
                //当队列中有数据时，不断执行取数据操作
                while (logQueue.peek() != null) {
                    isRunning = true;
                    logBeanList.add(logQueue.poll());//取出数据放到集合中
                    if (logBeanList.size() >= 20) {//当取出的数据大于等于20条时执行插入数据库操作
                        FTTrack.getInstance().logBackgroundSync(logBeanList);
                        logBeanList.clear();//插入完成后执行清除集合操作
                    }
                    Thread.sleep(100);
                }
                if (logBeanList.size() > 0) {//当队列中数据都取完后，且集合中没有20条数据
                    FTTrack.getInstance().logBackgroundSync(logBeanList);
                    logBeanList.clear();
                }
            } catch (Exception e){}finally {
                isRunning = false;
            }
            return true;
        });
        ThreadPoolUtils.get().execute(futureTask);
    }
}
